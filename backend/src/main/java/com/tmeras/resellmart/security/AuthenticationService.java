package com.tmeras.resellmart.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmeras.resellmart.email.EmailService;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.ResourceAlreadyExistsException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
import com.tmeras.resellmart.token.Token;
import com.tmeras.resellmart.token.TokenRepository;
import com.tmeras.resellmart.token.TokenType;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MfaService mfaService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpirationTime;

    @Transactional(rollbackFor = MessagingException.class)
    public AuthenticationResponse register(RegistrationRequest registrationRequest) throws MessagingException {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("USER role was not found"));
        if (userRepository.existsByEmail(registrationRequest.getEmail()))
            throw new ResourceAlreadyExistsException(
                    "A user with the email '" + registrationRequest.getEmail() + "' already exists"
            );

        User user = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(Set.of(userRole))
                .homeCountry(registrationRequest.getHomeCountry())
                .registeredAt(LocalDate.now())
                .enabled(false)
                .mfaEnabled(registrationRequest.isMfaEnabled())
                .build();

        // Generate secret and QR code image if MFA is enabled
        String qrImageUri = null;
        if (registrationRequest.isMfaEnabled()) {
            user.setSecret(mfaService.generateSecret());
            qrImageUri = mfaService.generateQrCodeImageUri(user.getSecret(), user.getEmail());
        }

        userRepository.save(user);
        sendActivationEmail(user);

        return AuthenticationResponse.builder()
                .qrImageUri(qrImageUri)
                .mfaEnabled(registrationRequest.isMfaEnabled())
                .build();
    }

    private void sendActivationEmail(User user) throws MessagingException {
        String activationCode = generateAndSaveActivationCode(user);

        emailService.sendActivationEmail(
                user.getEmail(),
                user.getRealName(),
                activationUrl + activationCode,
                activationCode
        );
    }

    private String generateAndSaveActivationCode(User user) {
        String generatedCode = generateActivationCode(6);
        tokenRepository.save(Token.builder()
                .token(generatedCode)
                .tokenType(TokenType.ACTIVATION)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .revoked(false)
                .user(user)
                .build()
        );

        return generatedCode;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();

        // Client needs to send OTP
        if (user.isMfaEnabled())
            return AuthenticationResponse.builder()
                    .mfaEnabled(true)
                    .build();

        return generateTokens(user);
    }

    @Transactional(
            rollbackFor = MessagingException.class,
            noRollbackFor = APIException.class
    )
    public void activateAccount(String code) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(code)
                .orElseThrow(() -> new ResourceNotFoundException("Activation token not found"));

        // TODO: Verify that token is of ACTIVATION type

        //  Return if token has already been validated
        if (savedToken.getValidatedAt() != null)
            return;

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendActivationEmail(savedToken.getUser());
            throw new APIException("Activation code has expired. A new email has been sent");
        }

        User user = savedToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

    public void refreshToken(String refreshToken, HttpServletResponse response) throws IOException {
        if (refreshToken.isEmpty())
            throw new JwtException("No refresh token was provided");

        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findWithAssociationsByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User with the email '" + userEmail + "' does not exist"));

            boolean isTokenRevoked = tokenRepository.findByToken(refreshToken)
                    .map(Token::isRevoked)
                    .orElseThrow(() -> new ResourceNotFoundException("Refresh token was not found"));

            if (!isTokenRevoked && jwtService.isTokenValid(refreshToken, user)) {
                Map<String, Object> claims = new HashMap<>();
                claims.put("name", user.getRealName());
                String newAccessToken = jwtService.generateAccessToken(claims, user);

                AuthenticationResponse authenticationResponse =
                        AuthenticationResponse.builder()
                                .accessToken(newAccessToken)
                                .build();

                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getOutputStream(), authenticationResponse);
                return;
            }
        }
        throw new JwtException("Invalid refresh token");
    }

    public AuthenticationResponse verifyOtp(VerificationRequest verificationRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        verificationRequest.getEmail(),
                        verificationRequest.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();

        if (!mfaService.isOtpValid(user.getSecret(), verificationRequest.getOtp()))
            throw new BadCredentialsException("OTP is not valid");

        return generateTokens(user);
    }

    private AuthenticationResponse generateTokens(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", user.getRealName());
        String accessToken = jwtService.generateAccessToken(claims, user);
        String refreshToken = jwtService.generateRefreshToken(user);
        tokenRepository.save(Token.builder()
                .token(refreshToken)
                .tokenType(TokenType.BEARER)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(refreshExpirationTime, ChronoUnit.MILLIS))
                .revoked(false)
                .user(user)
                .build()
        );

        // Include refresh token in HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(false) //TODO: Use secure in prod?
                .path("/")
                .maxAge(refreshExpirationTime / 1000)
                .build();

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshTokenCookie(refreshCookie.toString())
                .mfaEnabled(user.isMfaEnabled())
                .build();
    }
}
