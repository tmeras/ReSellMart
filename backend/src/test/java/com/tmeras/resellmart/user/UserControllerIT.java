package com.tmeras.resellmart.user;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class UserControllerIT {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final TestRestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // Used to add JWT in requests
    private HttpHeaders headers;

    private User userA;
    private User userB;
    private UserRequest userRequestA;
    private Product productA;
    private Product productB;

    @Autowired
    public UserControllerIT(
            TestRestTemplate restTemplate, PasswordEncoder passwordEncoder,
            JwtService jwtService, RoleRepository roleRepository,
            UserRepository userRepository, CategoryRepository categoryRepository,
            ProductRepository productRepository
    ) {
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @BeforeEach
    public void setUp() {
        // Empty relevant database tables
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Save required entities (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        userA = TestDataUtils.createUserA(Set.of(adminRole));
        userA.setId(null);
        userA.setPassword(passwordEncoder.encode(userA.getPassword()));
        userA = userRepository.save(userA);
        userRequestA = TestDataUtils.createUserRequestA();

        Role userRole = roleRepository.save(Role.builder().name("USER").build());
        userB = TestDataUtils.createUserB(Set.of(userRole));
        userB.setId(null);
        userB.setPassword(passwordEncoder.encode(userB.getPassword()));
        userB = userRepository.save(userB);

        Category category = TestDataUtils.createCategoryA();
        category.setId(null);
        category = categoryRepository.save(category);

        // Each product should have a different seller to test different scenarios
        productA = TestDataUtils.createProductA(category, userA);
        productA.setId(null);
        productA = productRepository.save(productA);

        productB = TestDataUtils.createProductB(category, userB);
        productB.setId(null);
        productB = productRepository.save(productB);

        // Generate test JWT with admin user details to include in each authenticated request
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), userA);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testJwt);
    }

    @Test
    public void shouldFindUserWhenValidUserId() {
        ResponseEntity<UserResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(userA.getRealName());
        assertThat(response.getBody().getEmail()).isEqualTo(userA.getEmail());
        assertThat(response.getBody().getRoles().stream().findFirst().get().getName()).isEqualTo("ADMIN");
        assertThat(response.getBody().getHomeCountry()).isEqualTo(userA.getHomeCountry());
    }

    @Test
    public void shouldNotFindUserWhenInvalidUserId() {
        ResponseEntity<UserResponse> response =
                restTemplate.exchange("/api/users/99", HttpMethod.GET,
                        new HttpEntity<>(headers), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFindAllUsers() {
        ResponseEntity<PageResponse<UserResponse>> response =
                restTemplate.exchange("/api/users", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(userA.getRealName());
        assertThat(response.getBody().getContent().get(1).getName()).isEqualTo(userB.getRealName());
    }

    @Test
    public void shouldNotFindAllUsersWhenNonAdminUser() {
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), userB);
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<?> response =
                restTemplate.exchange("/api/users", HttpMethod.GET,
                        new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }



}
