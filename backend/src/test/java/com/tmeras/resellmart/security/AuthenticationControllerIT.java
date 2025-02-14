package com.tmeras.resellmart.security;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.exception.ExceptionResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.Token;
import com.tmeras.resellmart.token.TokenRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import com.tmeras.resellmart.user.UserRequest;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    private User userA;

    @Autowired
    public AuthenticationControllerIT(
            TestRestTemplate restTemplate, RoleRepository roleRepository,
            UserRepository userRepository, TokenRepository tokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.restTemplate = restTemplate;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
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
                .mfaEnabled(true)
                .homeCountry("Australia")
                .build();

        ResponseEntity<AuthenticationResponse> response =
                restTemplate.exchange("/api/auth/register", HttpMethod.POST,
                        new HttpEntity<>(registrationRequest), AuthenticationResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQrImageUri()).isNotNull();
        assertThat(response.getBody().getMfaEnabled()).isTrue();

        Optional<User> createdUser = userRepository.findWithAssociationsByEmail(registrationRequest.getEmail());
        assertThat(createdUser).isNotEmpty();
        assertThat(createdUser.get().getRealName()).isEqualTo(registrationRequest.getName());
        assertThat(createdUser.get().getEmail()).isEqualTo(registrationRequest.getEmail());
        assertThat(createdUser.get().getHomeCountry()).isEqualTo(registrationRequest.getHomeCountry());
        assertThat(createdUser.get().getSecret()).isNotNull();

        Optional<Token> generatedActivationCode = tokenRepository.findActivationCodeByUserEmail(registrationRequest.getEmail());
        assertThat(generatedActivationCode).isNotEmpty();
    }

    @Test
    public void shouldNotRegisterUserWhenInvalidRequest() {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .name(null)
                .email("test@test.com")
                .password("Pass123!")
                .mfaEnabled(true)
                .homeCountry("Australia")
                .build();
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("name", "Name must not be empty");

        ResponseEntity<Map<String,String>> response =
                restTemplate.exchange("/api/auth/register", HttpMethod.POST,
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
                .mfaEnabled(true)
                .homeCountry("Australia")
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/register", HttpMethod.POST,
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
                .mfaEnabled(true)
                .homeCountry("Australia")
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/auth/register", HttpMethod.POST,
                        new HttpEntity<>(registrationRequest), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("A user with the email '" + userA.getEmail() + "' already exists");
    }



}
