package com.tmeras.resellmart.category;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CategoryControllerIT {

    @Container
    @ServiceConnection
    static MySQLContainer mysqlContainer = new MySQLContainer("mysql:9.2.0");

    private final TestRestTemplate restTemplate;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CategoryRepository categoryRepository;

    // Used to add include JWT in requests
    private HttpHeaders headers;

    @Autowired
    public CategoryControllerIT(
            TestRestTemplate restTemplate, CategoryRepository categoryRepository,
            UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService
    ) {
        this.restTemplate = restTemplate;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @BeforeEach
    public void setUp() {
        // Empty database tables
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Save required entities and an admin user
        categoryRepository.save(TestDataUtils.createCategoryA());
        categoryRepository.save(TestDataUtils.createCategoryB());
        Role savedAdminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        User savedAdminUser = userRepository.save(
                User.builder()
                        .name("Test admin user")
                        .password(passwordEncoder.encode("password"))
                        .email("test@test.com")
                        .roles(Set.of(savedAdminRole))
                        .build()
        );

        // Generate test JWT with admin user details to include in each authenticated request
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), savedAdminUser);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testJwt);
    }

    @Test
    public void containerRunning() {
        assertThat(mysqlContainer.isCreated()).isTrue();
        assertThat(mysqlContainer.isRunning()).isTrue();
    }

    @Test
    public void shouldCreateCategoryWhenValidCategory() {
        CategoryRequest categoryRequest = CategoryRequest.builder().name("New category").build();

        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories", HttpMethod.POST, new HttpEntity<>(categoryRequest, headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("New category");
    }

    @Test
    public void shouldFindAllCategories() {
        ResponseEntity<PageResponse<CategoryResponse>> response =
                restTemplate.exchange("/api/categories", HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
    }



}