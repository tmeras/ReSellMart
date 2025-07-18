package com.tmeras.resellmart.security;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.exception.ExceptionResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
import com.tmeras.resellmart.token.Token;
import com.tmeras.resellmart.token.TokenRepository;
import com.tmeras.resellmart.token.TokenType;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AuthenticationControllerIT {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final TestRestTemplate restTemplate;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private User userA;
    private String userPassword;

    @Autowired
    public AuthenticationControllerIT(
            TestRestTemplate restTemplate, RoleRepository roleRepository,
            UserRepository userRepository, TokenRepository tokenRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService
    ) {
        this.restTemplate = restTemplate;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @BeforeEach
    public void setUp() {
        // Empty relevant database tables
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Save required entities (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        roleRepository.save(Role.builder().name("USER").build());

        userA = TestDataUtils.createUserA(Set.of(adminRole));
        userPassword = userA.getPassword();
        userA.setId(null);
        userA.setPassword(passwordEncoder.encode(userA.getPassword()));
        userA = userRepository.save(userA);
    }

    @Test
    public void shouldRegisterUserWhenValidRequest() {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .name("Test user")
                .email("test@test.com")
                .password("Pass123!")
                .isMfaEnabled(true)
                .homeCountry("Australia")
                .build();

        ResponseEntity<AuthenticationResponse> response =
                restTemplate.exchange("/api/auth/registration", HttpMethod.POST,
                        new HttpEntity<>(registrationRequest), AuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQrImageUri()).isNotNull();
        assertThat(response.getBody().getIsMfaEnabled()).isTrue();

        Optional<User> createdUser = userRepository.findWithAssociationsByEmail(registrationRequest.getEmail());
        assertThat(createdUser).isNotEmpty();
        assertThat(createdUser.get().getRealName()).isEqualTo(registrationRequest.getName());
        assertThat(createdUser.get().getEmail()).isEqualTo(registrationRequest.getEmail());
        assertThat(createdUser.get().getHomeCountry()).isEqualTo(registrationRequest.getHomeCountry());
        assertThat(createdUser.get().getSecret()).isNotNull();

        Optional<Token> generatedActivationToken = tokenRepository.findActivationTokenByUserEmail(registrationRequest.getEmail());
        assertThat(generatedActivationToken).isNotEmpty();
    }

    @Test
    public void shouldNotRegisterUserWhenInvalidRequest() {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .name(null)
                .email("test@test.com")
                .password("Pass123!")
                .isMfaEnabled(true)
                .homeCountry("Australia")
                .build();
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("name", "Name must not be empty");

        ResponseEntity<Map<String,String>> response =
                restTemplate.exchange("/api/auth/registration", HttpMethod.POST,
                        new HttpEntity<>(registrationRequest), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedErrors);
    }

    @Test
    public void shouldNotRegisterUserWhenUserRoleDoesNotExist() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .name("Test user")
                .email("test@test.com")
                .password("Pass123!")
                .isMfaEnabled(true)
                .homeCountry("Australia")
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/registration", HttpMethod.POST,
                        new HttpEntity<>(registrationRequest), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("USER role was not found");
    }

    @Test
    public void shouldNotRegisterUserWhenDuplicateEmail() {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .name("Test user")
                .email(userA.getEmail())
                .password("Pass123!")
                .isMfaEnabled(true)
                .homeCountry("Australia")
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/registration", HttpMethod.POST,
                        new HttpEntity<>(registrationRequest), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("A user with the email '" + userA.getEmail() + "' already exists");
    }

    @Test
    public void shouldLoginUserWhenValidRequest() {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(userA.getEmail(), userPassword);

        ResponseEntity<AuthenticationResponse> response =
                restTemplate.exchange("/api/auth/login", HttpMethod.POST,
                        new HttpEntity<>(authenticationRequest), AuthenticationResponse.class);
        List<String> responseCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotNull();
        assertThat(tokenRepository.findAllValidRefreshTokensByUserEmail(userA.getEmail()).size()).isEqualTo(1);
        assertThat(jwtService.isTokenValid(response.getBody().getAccessToken(), userA)).isTrue();
        assertThat(responseCookies).isNotNull();
        String refreshToken = responseCookies.stream()
                .filter(cookie -> cookie.startsWith("refresh-token="))
                .map(cookie -> cookie.split(";")[0].split("=")[1])  // Extract the value
                .findFirst()
                .orElse(null);
        assertThat(refreshToken).isNotNull();
    }

    @Test
    public void shouldNotLoginUserWhenInvalidCredentials() {
        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(userA.getEmail(), "wrongPassword");

        ResponseEntity<AuthenticationResponse> response =
                restTemplate.exchange("/api/auth/login", HttpMethod.POST,
                        new HttpEntity<>(authenticationRequest), AuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldNotLoginUserWhenUserIsDisabled() {
        userA.setIsEnabled(false);
        userRepository.save(userA);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(userA.getEmail(), userPassword);

        ResponseEntity<AuthenticationResponse> response =
                restTemplate.exchange("/api/auth/login", HttpMethod.POST,
                        new HttpEntity<>(authenticationRequest), AuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldNotLoginUserWhenMfaIsEnabled() {
        userA.setIsMfaEnabled(true);
        userRepository.save(userA);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(userA.getEmail(), userPassword);

        ResponseEntity<AuthenticationResponse> response =
                restTemplate.exchange("/api/auth/login", HttpMethod.POST,
                        new HttpEntity<>(authenticationRequest), AuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsMfaEnabled()).isTrue();
        assertThat(response.getBody().getAccessToken()).isNull();
    }

    @Test
    public void shouldActivateAccountWhenValidRequest() {
        userA.setIsEnabled(false);
        userRepository.save(userA);
        // Manually save activation code
        tokenRepository.save(new Token(null, "code", TokenType.ACTIVATION, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(2), null, false, userA));

        ResponseEntity<?> response =
                restTemplate.exchange("/api/auth/activation?code=code", HttpMethod.POST,
                       null, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.findById(userA.getId()).get().isEnabled()).isTrue();
        assertThat(tokenRepository.findByToken("code").get().getValidatedAt()).isNotNull();
    }

    @Test
    public void shouldNotActivateAccountWhenActivationTokenDoesNotExist() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/activation?code=code", HttpMethod.POST,
                        null, ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Activation token not found");
    }

    @Test
    public void shouldNotActivateAccountWhenTokenHasBeenValidated() {
        userA.setIsEnabled(false);
        userRepository.save(userA);
        // Manually save activation code
        tokenRepository.save(new Token(null, "code", TokenType.ACTIVATION, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(2), LocalDateTime.now(), false, userA));

        ResponseEntity<?> response =
                restTemplate.exchange("/api/auth/activation?code=code", HttpMethod.POST,
                        null, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.findById(userA.getId()).get().isEnabled()).isFalse();
    }

    @Test
    public void shouldNotActivateAccountWhenTokenIsExpired() {
        // Manually save activation code
        tokenRepository.save(new Token(null, "code", TokenType.ACTIVATION, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().minusMinutes(1), null, false, userA));

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/activation?code=code", HttpMethod.POST,
                        null, ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Activation code has expired. A new email has been sent");
    }

    @Test
    public void shouldRefreshTokenWhenValidRequest() {
        // Manually save refresh token
        String refreshToken = jwtService.generateRefreshToken(userA);
        tokenRepository.save(new Token(null, refreshToken, TokenType.BEARER, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(2), null, false, userA));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "refresh-token=" + refreshToken);

        ResponseEntity<AuthenticationResponse> response =
                restTemplate.exchange("/api/auth/refresh", HttpMethod.POST,
                        new HttpEntity<>(headers), AuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotNull();
        assertThat(jwtService.isTokenValid(response.getBody().getAccessToken(), userA)).isTrue();
    }

    @Test
    public void shouldNotRefreshTokenWhenMissingRefreshToken() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/refresh", HttpMethod.POST,
                        null, ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No refresh token was provided");
    }

    @Test
    public void shouldNotRefreshTokenWhenUserDoesNotExist() {
        User userB = TestDataUtils.createUserB(userA.getRoles());
        String refreshToken = jwtService.generateRefreshToken(userB);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "refresh-token=" + refreshToken);
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/refresh", HttpMethod.POST,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("User with the email '" + userB.getEmail() + "' does not exist");
    }

    @Test
    public void shouldNotRefreshTokenWhenRefreshTokenDoesNotExist() {
        String refreshToken = jwtService.generateRefreshToken(userA);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "refresh-token=" + refreshToken);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/refresh", HttpMethod.POST,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Refresh token was not found");
    }

    @Test
    public void shouldNotRefreshTokenWhenRefreshTokenIsRevoked() {
        // Manually save refresh token
        String refreshToken = jwtService.generateRefreshToken(userA);
        tokenRepository.save(new Token(null, refreshToken, TokenType.BEARER, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().minusMinutes(1), null, true, userA));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "refresh-token=" + refreshToken);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/refresh", HttpMethod.POST,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid refresh token");
    }
}
