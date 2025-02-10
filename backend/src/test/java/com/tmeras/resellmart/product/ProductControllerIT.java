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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "application.file.upload.product-images-path=./test-uploads/product-images",
                "application.file.upload.user-images-path=./test-uploads/user-images"
        }
)
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
    public void setUp() throws IOException {
        // Empty relevant database tables
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
        productRequestA = TestDataUtils.createProductRequestA(category.getId());

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
        // Delete test uploads directory, if it exists
        File uploadsDirectory = new File("test-uploads");
        if (uploadsDirectory.exists())
            FileUtils.deleteDirectory(uploadsDirectory);
    }

    @Test
    public void shouldSaveProductWhenValidRequest() {
        ProductRequest productRequest =
                new ProductRequest(3, "Test product C", "Description C",
                        50.0, 25.0,  ProductCondition.FAIR, 1,
                        true, productA.getCategory().getId());

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
    public void shouldNotSaveProductWhenInvalidRequest() {
        ProductRequest productRequest =
                new ProductRequest(3, null, "Description C",
                        50.0, 25.0,  ProductCondition.FAIR, 1,
                        true, productA.getCategory().getId());

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldNotSaveProductWhenInvalidCategoryId() {
        ProductRequest productRequest =
                new ProductRequest(3, "Test product C", "Description C",
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
                new ProductRequest(3, "Test product C", "Description C",
                        20.0, 25.0,  ProductCondition.FAIR, 1,
                        true, productA.getCategory().getId());

        ResponseEntity<ProductResponse> response =
                restTemplate.exchange("/api/products", HttpMethod.POST,
                        new HttpEntity<>(productRequest, headers), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldFindProductByIdWhenValidProductId() {
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
    public void shouldNotFindProductByIdWhenInvalidProductId() {
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
    public void shouldUpdateProductWhenValidRequest() {
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

    @Test
    public void shouldUploadProductImagesWhenValidRequest() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", new ClassPathResource("test_image_1.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));

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
        requestBody.add("images", new ClassPathResource("test_image_1.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));

        ResponseEntity<?> response =
                restTemplate.exchange("/api/products/99/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldNotUploadProductImagesWhenSellerIsNotLoggedIn() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", new ClassPathResource("test_image_1.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));

        ResponseEntity<?> response =
                restTemplate.exchange("/api/products/" + productB.getId() + "/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldNotUploadProductImagesWhenImageLimitExceeded() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", new ClassPathResource("test_image_1.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Maximum 5 images can be uploaded");
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
    public void shouldDisplayImageWhenValidRequest() {
        // First upload images
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", new ClassPathResource("test_image_1.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));

        ResponseEntity<ProductResponse> firstResponse =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ProductResponse.class);

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstResponse.getBody()).isNotNull();
        assertThat(firstResponse.getBody().getImages().size()).isEqualTo(2);


        // Then mark the first image for display
        List<ProductImageResponse> productImages = firstResponse.getBody().getImages();
        Integer imageId = productImages.get(0).getId();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ProductResponse> secondResponse =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images/" + imageId + "/set-display",
                HttpMethod.PATCH, new HttpEntity<>(headers), ProductResponse.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody().getImages().size()).isEqualTo(2);

        productImages = secondResponse.getBody().getImages();
        Optional<ProductImageResponse> imageResponse =
                productImages.stream().filter(it -> it.getId().equals(imageId)).findFirst();
        assertThat(imageResponse.isPresent()).isTrue();
        assertThat(imageResponse.get().isDisplayed()).isEqualTo(true);
    }

    @Test
    public void shouldNotDisplayImageWhenInvalidProductId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/99/images/1/set-display", HttpMethod.PATCH,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No product found with ID: 99");
    }

    @Test
    public void shouldNotDisplayImageWhenInvalidImageId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images/99/set-display",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No product image found with ID: 99");
    }

    @Test
    public void shouldNotDisplayImageWhenImageBelongsToDifferentProduct() {
        // First upload image as one user
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), productB.getSeller());
        headers.set("Authorization", "Bearer " + testJwt);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", new ClassPathResource("test_image_1.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));

        ResponseEntity<ProductResponse> firstResponse =
                restTemplate.exchange("/api/products/" + productB.getId() + "/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ProductResponse.class);

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstResponse.getBody()).isNotNull();
        assertThat(firstResponse.getBody().getImages().size()).isEqualTo(2);

        // Then request one of those images to be displayed
        // as another user and for a different product
        List<ProductImageResponse> productImages = firstResponse.getBody().getImages();
        Integer imageId = productImages.get(0).getId();
        headers.setContentType(MediaType.APPLICATION_JSON);
        testJwt = jwtService.generateAccessToken(new HashMap<>(), productA.getSeller());
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<ExceptionResponse> secondResponse =
                restTemplate.exchange("/api/products/" + productA.getId() + "/images/" + imageId + "/set-display",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody().getMessage()).isEqualTo("The image is related to a different product");
    }

    @Test
    public void shouldDisplayImageWhenSellerIsNotLoggedIn() {
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), productB.getSeller());
        headers.set("Authorization", "Bearer " + testJwt);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("images", new ClassPathResource("test_image_1.jpeg"));
        requestBody.add("images", new ClassPathResource("test_image_2.jpeg"));

        ResponseEntity<ProductResponse> firstResponse =
                restTemplate.exchange("/api/products/" + productB.getId() + "/images", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ProductResponse.class);

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstResponse.getBody()).isNotNull();
        assertThat(firstResponse.getBody().getImages().size()).isEqualTo(2);

        List<ProductImageResponse> productImages = firstResponse.getBody().getImages();
        Integer imageId = productImages.get(0).getId();
        headers.setContentType(MediaType.APPLICATION_JSON);
        testJwt = jwtService.generateAccessToken(new HashMap<>(), productA.getSeller());
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<ExceptionResponse> secondResponse =
                restTemplate.exchange("/api/products/" + productB.getId() + "/images/" + imageId + "/set-display",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody().getMessage()).isEqualTo("You do not have permission to manage images for this product");
    }

    @Test
    public void shouldDeleteProduct() {
        ResponseEntity<?> response =
                restTemplate.exchange("/api/products/" + productA.getId(), HttpMethod.DELETE,
                        new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
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
}
