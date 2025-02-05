package com.tmeras.resellmart.product;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.ExceptionResponse;
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

    private Product productA;
    private ProductRequest productRequestA;
    private Product productB;
    private ProductRequest productRequestB;

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
        User userA = TestDataUtils.createUserA(Set.of(adminRole));
        userA.setId(null);
        userA.setPassword(passwordEncoder.encode(userA.getPassword()));
        userA = userRepository.save(userA);

        User userB = TestDataUtils.createUserB(Set.of(adminRole));
        userB.setId(null);
        userB.setPassword(passwordEncoder.encode(userB.getPassword()));
        userB = userRepository.save(userB);

        Category category = TestDataUtils.createCategoryA();
        category.setId(null);
        category = categoryRepository.save(category);

        // Each product should have a different seller to test the different findAll endpoints
        productA = TestDataUtils.createProductA(category, userA);
        productA.setId(null);
        productA = productRepository.save(productA);
        productRequestA = TestDataUtils.createProductRequestA(category.getId());

        productB = TestDataUtils.createProductB(category, userB);
        productB.setId(null);
        productB = productRepository.save(productB);
        productRequestB = TestDataUtils.createProductRequestB(category.getId());

        // Generate test JWT with admin user details to include in each authenticated request
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), userA);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testJwt);
    }

    @Test
    public void shouldSaveProductWhenValidProduct() {
        ProductRequest productRequest =
                new ProductRequest(3, "Test Product C", "Description C",
                        50.0, 25.0,  ProductCondition.FAIR, 1,
                        true, productRequestA.getCategoryId());

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(productRequest.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(productRequest.getDescription());
        assertThat(response.getBody().getPrice()).isEqualTo(productRequest.getPrice());
        assertThat(response.getBody().getDiscountedPrice()).isEqualTo(productRequest.getDiscountedPrice());
        assertThat(response.getBody().getProductCondition()).isEqualTo(productRequest.getProductCondition());
        assertThat(response.getBody().getCategory().getName()).isEqualTo(productA.getCategory().getName());
        assertThat(response.getBody().getSeller().getEmail()).isEqualTo(productA.getSeller().getEmail());
    }

    @Test
    public void shouldNotSaveProductWhenInvalidProduct() {
        ProductRequest productRequest =
                new ProductRequest(3, null, "Description C",
                        50.0, 25.0,  ProductCondition.FAIR, 1,
                        true, productRequestA.getCategoryId());

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldNotSaveProductWhenInvalidCategoryId() {
        ProductRequest productRequest =
                new ProductRequest(3, "Test Product C", "Description C",
                        50.0, 25.0,  ProductCondition.FAIR, 1,
                        true, 99);

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldNotSaveProductWhenInvalidPrice() {
        ProductRequest productRequest =
                new ProductRequest(3, "Test Product C", "Description C",
                        20.0, 25.0,  ProductCondition.FAIR, 1,
                        true, productA.getCategory().getId());

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldFindProductWhenValidProductId() {
        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(productA.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(productA.getDescription());
        assertThat(response.getBody().getPrice()).isEqualTo(productA.getPrice());
        assertThat(response.getBody().getDiscountedPrice()).isEqualTo(productA.getDiscountedPrice());
        assertThat(response.getBody().getProductCondition()).isEqualTo(productA.getProductCondition());
        assertThat(response.getBody().getCategory().getName()).isEqualTo(productA.getCategory().getName());
        assertThat(response.getBody().getSeller().getEmail()).isEqualTo(productA.getSeller().getEmail());
    }

    @Test
    public void shouldNotFindProductWhenInvalidProductId() {
        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products/99", HttpMethod.GET,
                        new HttpEntity<>(headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFindAllProducts() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
    }

    @Test
    public void shouldFindAllProductsExceptSeller() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/others", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productB.getName());
    }

    @Test
    public void shouldFindAllProductsBySellerId() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/user/" + productA.getSeller().getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productA.getName());
    }

    @Test
    public void shouldFindAllProductsByCategoryId() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/category/" + productA.getCategory().getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productB.getName());
    }

    @Test
    public void shouldFindAllProductsByKeyword() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/search?keyword=Test Product", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productB.getName());
    }

    @Test
    public void shouldUpdateProductWhenValidProduct() {
        productRequestA.setName("Updated product A");
        productRequestA.setDescription("Updated description A");

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.PUT,
                        new HttpEntity<>(productRequestA, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(productRequestA.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(productRequestA.getDescription());
    }

    @Test
    public void shouldNotUpdateProductWhenInvalidCategoryId() {
        productRequestA.setCategoryId(99);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.PUT,
                        new HttpEntity<>(productRequestA, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No category found with ID: 99");
    }

    @Test
    public void shouldNotUpdateProductWhenInvalidProductId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/99", HttpMethod.PUT,
                        new HttpEntity<>(productRequestA, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No product found with ID: 99");
    }

    @Test
    public void shouldNotUpdateProductWhenSellerIsNotLoggedIn() {
        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products/" + productB.getId(), HttpMethod.PUT,
                        new HttpEntity<>(productRequestA, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldNotUpdateProductWhenInvalidPrice() {
        productRequestA.setPrice(productRequestA.getDiscountedPrice() - 1);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.PUT,
                        new HttpEntity<>(productRequestA, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }



}
