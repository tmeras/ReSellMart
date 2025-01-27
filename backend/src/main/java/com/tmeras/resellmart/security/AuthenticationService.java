package com.tmeras.resellmart.security;

import com.tmeras.resellmart.email.EmailService;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.ResourceAlreadyExistsException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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
        Token token = Token.builder()
                .token(generatedCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        tokenRepository.save(token);
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

        Map<String, Object> claims = new HashMap<>();
        User user = (User) authentication.getPrincipal();
        claims.put("name", user.getRealName());
        String jwt = jwtService.generateToken(claims, user);

        return AuthenticationResponse.builder()
                .token(jwt)
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
}
