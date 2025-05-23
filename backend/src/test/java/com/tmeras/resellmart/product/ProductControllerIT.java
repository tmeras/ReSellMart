package com.tmeras.resellmart.product;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.address.AddressRepository;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.ExceptionResponse;
import com.tmeras.resellmart.order.OrderRepository;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
public class ProductControllerIT {

    private static final ClassPathResource TEST_IMAGE_1 = new ClassPathResource("test_image_1.jpeg");
    private static final ClassPathResource TEST_IMAGE_2 = new ClassPathResource("test_image_2.jpeg");

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
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;

    // Used to add JWT in requests
    private HttpHeaders headers;

    private Product productA;
    private Product productB;
    private ProductUpdateRequest productUpdateRequestA;

    @Autowired
    public ProductControllerIT(
            TestRestTemplate restTemplate, UserRepository userRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, CategoryRepository categoryRepository,
            ProductRepository productRepository, OrderRepository orderRepository,
            AddressRepository addressRepository
    ) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
    }

    @BeforeEach
    public void setUp() throws IOException {
        // Empty relevant database tables
        orderRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Save required entities (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        User userA = TestDataUtils.createUserA(Set.of(adminRole));
        userA.setId(null);
        userA.setPassword(passwordEncoder.encode(userA.getPassword()));
        userA = userRepository.save(userA);

        Role userRole = roleRepository.save(Role.builder().name("USER").build());
        User userB = TestDataUtils.createUserB(Set.of(userRole));
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
        productUpdateRequestA = ProductUpdateRequest.builder()
                .name("Updated test product A")
                .description("Updated description A")
                .price(BigDecimal.valueOf(10.00))
                .productCondition(ProductCondition.NEW)
                .availableQuantity(2)
                .categoryId(category.getId())
                .isDeleted(false)
                .build();

        productB = TestDataUtils.createProductB(category, userB);
        productB.setId(null);
        productB = productRepository.save(productB);

        // Generate test JWT with admin user details to include in each authenticated request
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), userA);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testJwt);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        File uploadsDirectory = new File("test-uploads");
        if (uploadsDirectory.exists())
            FileUtils.deleteDirectory(uploadsDirectory);
    }

    @Test
    public void shouldSaveProductWhenValidRequest() {
        ProductRequest productRequest =
                new ProductRequest(3, "Test product C", "Description C",
                        BigDecimal.valueOf(50.0), ProductCondition.FAIR, 1, productA.getCategory().getId());

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(productRequest.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(productRequest.getDescription());
        assertThat(response.getBody().getPrice()).isEqualTo(productRequest.getPrice());
        assertThat(response.getBody().getCondition()).isEqualTo(productRequest.getCondition());
        assertThat(response.getBody().getCategory().getName()).isEqualTo(productA.getCategory().getName());
        assertThat(response.getBody().getSeller().getEmail()).isEqualTo(productA.getSeller().getEmail());
    }

    @Test
    public void shouldNotSaveProductWhenInvalidRequest() {
        ProductRequest productRequest =
                new ProductRequest(3, null, "Description C",
                        BigDecimal.valueOf(50.0), ProductCondition.FAIR, 1, productA.getCategory().getId());
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("name", "Name must not be empty");

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedErrors);
    }

    @Test
    public void shouldNotSaveProductWhenInvalidCategoryId() {
        ProductRequest productRequest =
                new ProductRequest(3, "Test product C", "Description C",
                        BigDecimal.valueOf(50.0), ProductCondition.FAIR, 1, 99);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No category found with ID: 99");
    }

    @Test
    public void shouldNotSaveProductWhenInvalidQuantity() {
        ProductRequest productRequest =
                new ProductRequest(3, "Test product C", "Description C",
                        BigDecimal.valueOf(50.0), ProductCondition.FAIR, 0, productA.getCategory().getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Quantity of newly created product must be greater than be 0");
    }

    @Test
    public void shouldFindProductByIdWhenValidProductId() {
        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), ProductResponse.class);

        System.out.println(productA.getPrice());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(productA.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(productA.getDescription());
        assertThat(response.getBody().getPrice().compareTo(productA.getPrice())).isEqualTo(0);
        assertThat(response.getBody().getPreviousPrice().compareTo(productA.getPreviousPrice())).isEqualTo(0);
        assertThat(response.getBody().getCondition()).isEqualTo(productA.getCondition());
        assertThat(response.getBody().getCategory().getName()).isEqualTo(productA.getCategory().getName());
        assertThat(response.getBody().getSeller().getEmail()).isEqualTo(productA.getSeller().getEmail());
    }

    @Test
    public void shouldNotFindProductByIdWhenInvalidProductId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/99", HttpMethod.GET,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No product found with ID: 99");
    }

    @Test
    public void shouldFindAllProducts() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productA.getName());
        assertThat(response.getBody().getContent().get(1).getName()).isEqualTo(productB.getName());
    }

    @Test
    public void shouldNotFindAllProductsWhenNonAdminUser() {
        // Generate JWT with non-admin user details
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), productB.getSeller());
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<?> response =
                restTemplate.exchange("/api/products", HttpMethod.GET,
                        new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldFindAllProductsExceptSellerProducts() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/others", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productB.getName());
    }

    @Test
    public void shouldFindAllProductsByKeywordExceptSellerProducts() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/others?search=Test product", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productB.getName());
    }

    @Test
    public void shouldFindAllProductsBySellerId() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/users/" + productA.getSeller().getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productA.getName());
    }

    @Test
    public void shouldFindAllProductsBySellerIdAndKeyword() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/users/" + productA.getSeller().getId() + "?search=Test product",
                        HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productA.getName());
    }

    @Test
    public void shouldFindAllProductsByCategoryId() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/categories/" + productA.getCategory().getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productB.getName());
    }

    @Test
    public void shouldFindAllProductsByCategoryIdAndKeyword() {
        ResponseEntity<PageResponse<ProductResponse>> response =
                restTemplate.exchange("/api/products/categories/" + productA.getCategory().getId() + "?search=Test product",
                        HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(productB.getName());
    }

    @Test
    public void shouldUpdateProductWhenValidRequest() {
        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.PATCH,
                        new HttpEntity<>(productUpdateRequestA, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(productUpdateRequestA.getName());
        assertThat(response.getBody().getDescription()).isEqualTo(productUpdateRequestA.getDescription());
    }

    @Test
    public void shouldNotUpdateProductWhenInvalidCategoryId() {
        productUpdateRequestA.setCategoryId(99);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.PATCH,
                        new HttpEntity<>(productUpdateRequestA, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No category found with ID: 99");
    }

    @Test
    public void shouldNotUpdateProductWhenInvalidProductId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/99", HttpMethod.PATCH,
                        new HttpEntity<>(productUpdateRequestA, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No product found with ID: 99");
    }

    @Test
    public void shouldNotUpdateProductWhenSellerIsNotLoggedIn() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productB.getId(), HttpMethod.PATCH,
                        new HttpEntity<>(productUpdateRequestA, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("You do not have permission to update this product");
    }

    @Test
    public void shouldUploadProductImagesWhenValidRequest() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", TEST_IMAGE_1);
        requestBody.add("images", TEST_IMAGE_2);

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getImages().size()).isEqualTo(2);
    }

    @Test
    public void shouldNotUploadProductImagesWhenInvalidProductId() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images",TEST_IMAGE_1);
        requestBody.add("images", TEST_IMAGE_2);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/99/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No product found with ID: 99");
    }

    @Test
    public void shouldNotUploadProductImagesWhenSellerIsNotLoggedIn() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", TEST_IMAGE_1);
        requestBody.add("images", TEST_IMAGE_2);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productB.getId() + "/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("You do not have permission to upload images for this product");
    }

    @Test
    public void shouldNotUploadProductImagesWhenImageLimitExceeded() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", TEST_IMAGE_1);
        for (int i = 0; i < AppConstants.MAX_IMAGE_NUMBER; i++)
            requestBody.add("images", TEST_IMAGE_2);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Maximum " + AppConstants.MAX_IMAGE_NUMBER + " images can be uploaded");
    }

    @Test
    public void shouldNotUploadProductImagesWhenInvalidFileExtension() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", new ClassPathResource("test_file.txt"));

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Only images can be uploaded");
    }

    @Test
    public void shouldFindPrimaryProductImageWhenValidRequest() throws IOException {
        File testFile = TEST_IMAGE_1.getFile();
        ProductImage productImage =
                new ProductImage(null, testFile.getName(), "image/jpeg", testFile.getAbsolutePath());
        productA.setImages(List.of(productImage));
        productRepository.save(productA);

        ResponseEntity<byte[]> response =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images/primary", HttpMethod.GET,
                        new HttpEntity<>(headers), byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void shouldNotFindPrimaryProductImageWhenEmptyImageList() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images/primary", HttpMethod.GET,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No images found for product with ID: " + productA.getId());
    }

    @Test
    public void shouldDeleteProduct() {
        ResponseEntity<?> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.DELETE,
                        new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(productRepository.findById(productA.getId()).get().getIsDeleted()).isTrue();
    }

    @Test
    public void shouldNotDeleteProductWhenNonAdminUser() {
        // Generate JWT with non-admin user details
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), productB.getSeller());
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<?> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.DELETE,
                        new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldNotDeleteProductWhenInvalidProductId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/99", HttpMethod.DELETE,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("No product found with ID: 99");
    }
}
