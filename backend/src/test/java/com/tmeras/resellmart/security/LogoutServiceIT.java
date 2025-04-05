package com.tmeras.resellmart.security;

import com.tmeras.resellmart.TestDataUtils;
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
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class LogoutServiceIT {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final TestRestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    // Used to add JWT in requests
    private HttpHeaders headers;

    private User user;
    private Token refreshToken;

    @BeforeEach
    public void setUp() {
        // Empty relevant database tables
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Save required entities (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        user = TestDataUtils.createUserA(Set.of(adminRole));
        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);

        refreshToken = new Token(null, jwtService.generateRefreshToken(user), TokenType.BEARER,
                LocalDateTime.now().minusMinutes(2), LocalDateTime.now().plusMinutes(1), null, false, user);
        refreshToken = tokenRepository.save(refreshToken);

        // Generate test JWT with admin user details to include in each authenticated request
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), user);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testJwt);
    }

    @Autowired
    public LogoutServiceIT(
            TestRestTemplate restTemplate, PasswordEncoder passwordEncoder,
            JwtService jwtService, RoleRepository roleRepository,
            UserRepository userRepository, TokenRepository tokenRepository
    ) {
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Test
    public void shouldLogoutUserWhenValidRequest() {
        ResponseEntity<?> response =
                restTemplate.exchange("/api/auth/logout", HttpMethod.POST,
                        new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(tokenRepository.findByToken(refreshToken.getToken()).get().isRevoked()).isTrue();
    }

    @Test
    public void shouldNotLogoutUserWhenMissingAccessToken() {
        headers.remove("Authorization");
        String expectedResponse = "{\"error\": \"No access token in Bearer header\"}";

        ResponseEntity<String> response =
                restTemplate.exchange("/api/auth/logout", HttpMethod.POST,
                        new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldNotLogoutUserWhenInvalidAccessToken() {
        headers.set("Authorization", "Bearer  invalidToken");
        String expectedResponse = "{\"error\": \"Invalid access token\"}";

        ResponseEntity<String> response =
                restTemplate.exchange("/api/auth/logout", HttpMethod.POST,
                        new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }
}
