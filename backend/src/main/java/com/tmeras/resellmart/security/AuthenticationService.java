package com.tmeras.resellmart.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmeras.resellmart.email.EmailService;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.ResourceAlreadyExistsException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
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

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpirationTime;

    public void register(RegistrationRequest registrationRequest) throws MessagingException {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("USER role was not found"));

        if (userRepository.existsByEmail(registrationRequest.getEmail()))
            throw new ResourceAlreadyExistsException(
                    "A user with the email \"" + registrationRequest.getEmail() + "\" already exists"
            );

        User user = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .roles(Set.of(userRole))
                .homeCountry(registrationRequest.getHomeCountry())
                .dob(registrationRequest.getDob())
                .enabled(false)
                .build();

        userRepository.save(user);
        sendActivationEmail(user);
    }

    private void sendActivationEmail(User user) throws MessagingException {
        String activationCode = generateAndSaveActivationCode(user);

        emailService.sendActivationEmail(
                user.getEmail(),
                "ReSellMart Account Activation",
                user.getRealName(),
                activationUrl,
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

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional(
            rollbackOn = MessagingException.class,
            dontRollbackOn = APIException.class
    )
    public void activateAccount(String code) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(code)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

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

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new JwtException("No refresh token in Bearer header");

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User with the email \"" + userEmail + "\" not found"));

            boolean isTokenRevoked = tokenRepository.findByToken(refreshToken)
                    .map(Token::isRevoked)
                    .orElseThrow(() -> new JwtException("Refresh token not found"));

            if (!isTokenRevoked && jwtService.isTokenValid(refreshToken, user)) {
                Map<String, Object> claims = new HashMap<>();
                claims.put("name", user.getRealName());
                String newAccessToken = jwtService.generateAccessToken(claims, user);

                AuthenticationResponse authenticationResponse =
                        AuthenticationResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(refreshToken)
                                .build();

                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getOutputStream(), authenticationResponse);
                return;
            }
        }
        throw new JwtException("Invalid refresh token");
    }
}
