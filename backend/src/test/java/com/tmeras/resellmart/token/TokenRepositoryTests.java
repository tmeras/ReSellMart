package com.tmeras.resellmart.token;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class TokenRepositoryTests {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private User user;
    private Token activationToken;
    private Token validRefreshToken;
    private Token invalidRefreshToken;

    @Autowired
    public TokenRepositoryTests(
            TokenRepository tokenRepository, UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @BeforeEach
    public void setUp() {
        // Save required entities (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = new Role(null, "ADMIN");
        adminRole = roleRepository.save(adminRole);
        user = TestDataUtils.createUserA(Set.of(adminRole));
        user.setId(null);
        user = userRepository.save(user);

        activationToken = new Token(null, "activationCode", TokenType.ACTIVATION, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(2), null, false, user);
        activationToken = tokenRepository.save(activationToken);
        validRefreshToken = new Token(null, "validRefreshToken", TokenType.BEARER, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(2), null, false, user);
        validRefreshToken = tokenRepository.save(validRefreshToken);
        invalidRefreshToken = new Token(null, "invalidRefreshToken", TokenType.BEARER, LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(2), null, true, user);
        invalidRefreshToken = tokenRepository.save(invalidRefreshToken);
    }

    @Test
    public void shouldFindActivationTokenByUserEmail() {
        Optional<Token> token = tokenRepository.findActivationTokenByUserEmail(user.getEmail());

        assertThat(token.isPresent()).isTrue();
        assertThat(token.get().getTokenType()).isEqualTo(TokenType.ACTIVATION);
        assertThat(token.get().getToken()).isEqualTo(activationToken.getToken());
    }

    @Test
    public void shouldFindAllValidRefreshTokensByUserEmail() {
        List<Token> tokens = tokenRepository.findAllValidRefreshTokensByUserEmail(user.getEmail());

        assertThat(tokens.size()).isEqualTo(1);
        assertThat(tokens.get(0).getTokenType()).isEqualTo(TokenType.BEARER);
        assertThat(tokens.get(0).getToken()).isEqualTo(validRefreshToken.getToken());
        assertThat(tokens.get(0).getIsRevoked()).isFalse();
    }
}
