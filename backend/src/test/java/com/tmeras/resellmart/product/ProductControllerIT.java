package com.tmeras.resellmart.product;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
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

import java.util.HashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ProductControllerIT {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final TestRestTemplate restTemplate;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // TODO: Only one product and product request defined globally?
    private Product product;
    private ProductRequest productRequest;

    // Used to add include JWT in requests
    private HttpHeaders headers;

    @Autowired
    public ProductControllerIT(
            TestRestTemplate restTemplate, UserRepository userRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, CategoryRepository categoryRepository,
            ProductRepository productRepository
    ) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

        // Save required entities and an admin user (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        User user = TestDataUtils.createUserA(Set.of(adminRole));
        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);

        Category category = TestDataUtils.createCategoryA();
        category.setId(null);
        category = categoryRepository.save(category);

        product = TestDataUtils.createProductA(category, user);
        product.setId(null);
        product = productRepository.save(product);
        productRequest = TestDataUtils.createProductRequestA(category.getId());

        // Generate test JWT with admin user details to include in each authenticated request
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), user);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testJwt);
    }

    @Test
    public void shouldFindProductWhenValidProductId() {
        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products/" + product.getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(product.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(product.getDescription());
        assertThat(response.getBody().getPrice()).isEqualTo(product.getPrice());
        assertThat(response.getBody().getDiscountedPrice()).isEqualTo(product.getDiscountedPrice());
        assertThat(response.getBody().getProductCondition()).isEqualTo(product.getProductCondition());
        assertThat(response.getBody().getCategory().getName()).isEqualTo(product.getCategory().getName());
        assertThat(response.getBody().getSeller().getEmail()).isEqualTo(product.getSeller().getEmail());
    }

    @Test
    public void shouldNotFindProductWhenInvalidProductId() {
        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products/99", HttpMethod.GET,
                        new HttpEntity<>(headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }



}
