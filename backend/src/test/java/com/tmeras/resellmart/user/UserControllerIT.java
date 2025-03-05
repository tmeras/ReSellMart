package com.tmeras.resellmart.user;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.cart.CartItem;
import com.tmeras.resellmart.cart.CartItemRepository;
import com.tmeras.resellmart.cart.CartItemRequest;
import com.tmeras.resellmart.cart.CartItemResponse;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.ExceptionResponse;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
import com.tmeras.resellmart.token.Token;
import com.tmeras.resellmart.token.TokenRepository;
import com.tmeras.resellmart.token.TokenType;
import com.tmeras.resellmart.wishlist.WishListItem;
import com.tmeras.resellmart.wishlist.WishListItemRepository;
import com.tmeras.resellmart.wishlist.WishListItemRequest;
import com.tmeras.resellmart.wishlist.WishListItemResponse;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// TODO: Code coverage
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"application.file.upload.user-images-path=./test-uploads/user-images"}
)
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
    private final CartItemRepository cartItemRepository;
    private final WishListItemRepository wishListItemRepository;
    private final TokenRepository tokenRepository;

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
            ProductRepository productRepository, CartItemRepository cartItemRepository,
            WishListItemRepository wishListItemRepository, TokenRepository tokenRepository
    ) {
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.wishListItemRepository = wishListItemRepository;
        this.tokenRepository = tokenRepository;
    }

    @BeforeEach
    public void setUp() {
        // Empty relevant database tables
        wishListItemRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        tokenRepository.deleteAll();
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

    @AfterEach
    public void tearDown() throws IOException {
        File uploadsDirectory = new File("test-uploads");
        if (uploadsDirectory.exists())
            FileUtils.deleteDirectory(uploadsDirectory);
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

    @Test
    public void shouldUpdateUserWhenValidRequest() {
        userRequestA.setName("Updated user A");
        userRequestA.setHomeCountry("Updated home country");
        userRequestA.setMfaEnabled(true);

        ResponseEntity<UserResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId(), HttpMethod.PUT,
                        new HttpEntity<>(userRequestA, headers), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(userRequestA.getName());
        assertThat(response.getBody().getHomeCountry()).isEqualTo(userRequestA.getHomeCountry());
        assertThat(response.getBody().isMfaEnabled()).isEqualTo(userRequestA.isMfaEnabled());
        assertThat(response.getBody().getQrImageUri()).isNotNull();
    }

    @Test
    public void shouldNotUpdateUserWhenInvalidRequest() {
        userRequestA.setName(null);
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("name", "Name must not be empty");

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange("/api/users/" + userA.getId(), HttpMethod.PUT,
                        new HttpEntity<>(userRequestA, headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedErrors);
    }

    @Test
    public void shouldNotUpdateUserWheUserIsNotLoggedIn() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userB.getId(), HttpMethod.PUT,
                        new HttpEntity<>(userRequestA, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to update the details of this user");
    }

    @Test
    public void shouldUploadUserImageWhenValidRequest() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("image", new ClassPathResource("test_picture.png"));

        ResponseEntity<UserResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/image", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProfileImage()).isNotNull();
    }

    @Test
    public void shouldNotUploadUserImageWhenUserIsNotLoggedIn() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("image", new ClassPathResource("test_picture.png"));

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userB.getId() + "/image", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("You do not have permission to update this user's profile image");
    }

    @Test
    public void shouldNotUploadUserImageWhenInvalidFileExtension() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("image", new ClassPathResource("test_file.txt"));

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/image", HttpMethod.PUT,
                        new HttpEntity<>(requestBody, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Only images can be uploaded");
    }

    @Test
    public void shouldSaveCartItemWhenValidRequest() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 1, userA.getId());

        ResponseEntity<CartItemResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products", HttpMethod.POST,
                        new HttpEntity<>(cartItemRequest, headers), CartItemResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getProduct().getId()).isEqualTo(productB.getId());
        assertThat(response.getBody().getQuantity()).isEqualTo(cartItemRequest.getQuantity());
        assertThat(response.getBody().getAddedAt()).isNotNull();
    }

    @Test
    public void shouldNotSaveCartItemWhenInvalidRequest() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 0, userA.getId());
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("quantity", "Quantity must be a positive value");

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products", HttpMethod.POST,
                        new HttpEntity<>(cartItemRequest, headers), new ParameterizedTypeReference<Map<String, String>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedErrors);
    }

    @Test
    public void shouldNotSaveCartItemWhenCartOwnerIsNotLoggedIn() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 1, userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userB.getId() + "/cart/products", HttpMethod.POST,
                        new HttpEntity<>(cartItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to add items to this user's cart");
    }

    @Test
    public void shouldNotSaveCartItemWhenDuplicateCartItem() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 1, userA.getId());
        cartItemRepository.save(new CartItem(null, productB, 1, userA, LocalDateTime.now()));

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products", HttpMethod.POST,
                        new HttpEntity<>(cartItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("This product is already in your cart");
    }

    @Test
    public void shouldNotSaveCartItemWhenInvalidProductId() {
        CartItemRequest cartItemRequest = new CartItemRequest(99, 1, userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products", HttpMethod.POST,
                        new HttpEntity<>(cartItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("No product found with ID: 99");
    }

    @Test
    public void shouldNotSaveCartItemWhenSellerIsLoggedIn() {
        CartItemRequest cartItemRequest = new CartItemRequest(productA.getId(), 1, userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products", HttpMethod.POST,
                        new HttpEntity<>(cartItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You cannot add your own items to your cart");
    }

    @Test
    public void shouldNotSaveCartItemWhenProductIsDeleted() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 1, userA.getId());
        productB.setIsDeleted(true);
        productRepository.save(productB);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products", HttpMethod.POST,
                        new HttpEntity<>(cartItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Deleted products cannot be added to the cart");
    }

    @Test
    public void shouldNotSaveCartItemWhenInvalidQuantity() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 99, userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products", HttpMethod.POST,
                        new HttpEntity<>(cartItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Requested product quantity cannot be higher than available quantity");
    }

    @Test
    public void shouldFindAllCartItemsByUserIdWhenValidUserId() {
        cartItemRepository.save(new CartItem(null, productB, 1, userA, LocalDateTime.now()));

        ResponseEntity<List<CartItemResponse>> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(1);
        assertThat(response.getBody().get(0).getProduct().getName()).isEqualTo(productB.getName());
    }

    @Test
    public void shouldNotFindAllCartItemsByUserIdWhenCartOwnerIsNotLoggedIn() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + 99 + "/cart/products", HttpMethod.GET,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to view this user's cart");
    }

    @Test
    public void shouldUpdateCartItemQuantityWhenValidRequest() {
        cartItemRepository.save(new CartItem(null, productB, 1, userA, LocalDateTime.now()));
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 2, userA.getId());

        ResponseEntity<CartItemResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products/" + productB.getId(),
                        HttpMethod.PATCH, new HttpEntity<>(cartItemRequest, headers), CartItemResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQuantity()).isEqualTo(2);
    }

    @Test
    public void shouldNotUpdateCartItemQuantityWhenCartOwnerIsNotLoggedIn() {
        cartItemRepository.save(new CartItem(null, productB, 1, userA, LocalDateTime.now()));
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 2, userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userB.getId() + "/cart/products/"  + productB.getId(),
                        HttpMethod.PATCH, new HttpEntity<>(cartItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to modify this user's cart");
    }

    @Test
    public void shouldNotUpdateCartItemQuantityWhenCartItemDoesNotExist() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 2, userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products/" + productB.getId(),
                        HttpMethod.PATCH, new HttpEntity<>(cartItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("The specified product does not exist in your cart");
    }

    @Test
    public void shouldNotUpdateCartItemQuantityWhenInvalidQuantity() {
        cartItemRepository.save(new CartItem(null, productB, 1, userA, LocalDateTime.now()));
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 99, userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products/" + productB.getId(),
                        HttpMethod.PATCH, new HttpEntity<>(cartItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Requested product quantity cannot be higher than available quantity");
    }

    @Test
    public void shouldDeleteCartItemWhenValidRequest() {
        cartItemRepository.save(new CartItem(null, productB, 1, userA, LocalDateTime.now()));

        ResponseEntity<?> response = restTemplate.exchange("/api/users/" + userA.getId() + "/cart/products/" + productB.getId(),
                HttpMethod.DELETE, new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(cartItemRepository.count()).isEqualTo(0);
    }

    @Test
    public void shouldNotDeleteCartItemWhenCartOwnerIsNotLoggedIn() {
        cartItemRepository.save(new CartItem(null, productB, 1, userA, LocalDateTime.now()));

        ResponseEntity<ExceptionResponse> response = restTemplate.exchange("/api/users/" + userB.getId() + "/cart/products/" + productB.getId(),
                HttpMethod.DELETE, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to modify this user's cart");
    }

    @Test
    public void shouldSaveWishListItemWhenValidRequest() {
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productB.getId(), userA.getId());

        ResponseEntity<WishListItemResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/wishlist/products", HttpMethod.POST,
                        new HttpEntity<>(wishListItemRequest, headers), WishListItemResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProduct().getId()).isEqualTo(productB.getId());
    }

    @Test
    public void shouldNotSaveWishListItemWhenListOwnerIsNotLoggedIn() {
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productB.getId(), userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userB.getId() + "/wishlist/products", HttpMethod.POST,
                        new HttpEntity<>(wishListItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to add items to this user's wishlist");
    }

    @Test
    public void shouldNotSaveWishListItemWhenInvalidRequest() {
        WishListItemRequest wishListItemRequest = new WishListItemRequest(null, userA.getId());
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("productId", "Product ID must not be empty");

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/wishlist/products", HttpMethod.POST,
                        new HttpEntity<>(wishListItemRequest, headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedErrors);
    }

    @Test
    public void shouldNotSaveWishListItemWhenDuplicateWishlistItem() {
        wishListItemRepository.save(new WishListItem(null, LocalDateTime.now(), productB, userA));
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productB.getId(), userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/wishlist/products", HttpMethod.POST,
                        new HttpEntity<>(wishListItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("This product is already in your wishlist");
    }

    @Test
    public void shouldNotSaveWishListItemWhenInvalidProductId() {
        WishListItemRequest wishListItemRequest = new WishListItemRequest(99, userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/wishlist/products", HttpMethod.POST,
                        new HttpEntity<>(wishListItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("No product found with ID: 99");
    }

    @Test
    public void shouldNotSaveWishListItemWhenSellerIsLoggedIn() {
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productA.getId(), userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/wishlist/products", HttpMethod.POST,
                        new HttpEntity<>(wishListItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You cannot add your own items to your wishlist");
    }

    @Test
    public void shouldNotSaveWishListItemWhenProductIsUnavailable() {
        productB.setIsDeleted(true);
        productRepository.save(productB);
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productB.getId(), userA.getId());

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/wishlist/products", HttpMethod.POST,
                        new HttpEntity<>(wishListItemRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Deleted products cannot be added to the wishlist");
    }

    @Test
    public void shouldFindAllWishlistItemsByUserIdWhenValidUserId() {
        wishListItemRepository.save(new WishListItem(null, LocalDateTime.now(), productB, userA));

        ResponseEntity<List<WishListItemResponse>> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/wishlist/products", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(1);
        assertThat(response.getBody().get(0).getProduct().getId()).isEqualTo(productB.getId());
    }

    @Test
    public void shouldNotFindAllWishlistItemsByUserIdWhenListOwnerIsNotLoggedIn() {
        wishListItemRepository.save(new WishListItem(null, LocalDateTime.now(), productB, userA));

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + 99+ "/wishlist/products", HttpMethod.GET,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to view this user's wishlist");
    }

    @Test
    public void shouldDeleteWishListItemWhenValidRequest() {
        wishListItemRepository.save(new WishListItem(null, LocalDateTime.now(), productB, userA));

        ResponseEntity<?> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/wishlist/products/" + productB.getId(),
                        HttpMethod.DELETE, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(wishListItemRepository.count()).isEqualTo(0);
    }

    @Test
    public void shouldNotDeleteWishListItemWhenListOwnerIsNotLoggedIn() {
        wishListItemRepository.save(new WishListItem(null, LocalDateTime.now(), productB, userA));
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), userB);
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + userA.getId() + "/wishlist/products/" + productB.getId(),
                        HttpMethod.DELETE, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to modify this user's wishlist");
    }

    @Test
    public void shouldDisableUserWhenValidRequest() {
        UserEnableRequest userEnableRequest = new UserEnableRequest(false);
        // Downgrade admin user
        userA.setRoles(Set.of(userB.getRoles().stream().findFirst().get()));
        userRepository.save(userA);
        // Manually save refresh token for the relevant user
        Token testToken = new Token(null, "token", TokenType.BEARER, LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(1), null, false, userA);
        testToken = tokenRepository.save(testToken);

        ResponseEntity<?> response = restTemplate.exchange("/api/users/" + userA.getId() + "/activation",
                HttpMethod.PATCH, new HttpEntity<>(userEnableRequest, headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.findById(userA.getId()).get().isEnabled()).isFalse();
        assertThat(productRepository.findAllBySellerId(userA.getId()).get(0).getIsDeleted()).isFalse();
        assertThat(tokenRepository.findById(testToken.getId()).get().isRevoked()).isTrue();
    }

    @Test
    public void shouldNotDisableUserWhenUserOrAdminIsNotLoggedIn() {
        UserEnableRequest userEnableRequest = new UserEnableRequest(false);
        // Downgrade admin user
        userA.setRoles(Set.of(userB.getRoles().stream().findFirst().get()));
        userRepository.save(userA);

        ResponseEntity<ExceptionResponse> response = restTemplate.exchange("/api/users/" + userB.getId() + "/activation",
                HttpMethod.PATCH, new HttpEntity<>(userEnableRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to disable this user");
    }

    @Test
    public void shouldNotDisableUserWhenInvalidUserId() {
        UserEnableRequest userEnableRequest = new UserEnableRequest(false);

        ResponseEntity<ExceptionResponse> response = restTemplate.exchange("/api/users/" + 99 + "/activation",
                HttpMethod.PATCH, new HttpEntity<>(userEnableRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No user found with ID: 99");
    }

    @Test
    public void shouldNotDisableAdminUser() {
        UserEnableRequest userEnableRequest = new UserEnableRequest(false);

        ResponseEntity<ExceptionResponse> response = restTemplate.exchange("/api/users/" + userA.getId() + "/activation",
                HttpMethod.PATCH, new HttpEntity<>(userEnableRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("You cannot disable an admin user");
    }

    @Test
    public void shouldEnableUserWhenValidRequest() {
        UserEnableRequest userEnableRequest = new UserEnableRequest(true);
        userB.setEnabled(false);
        userRepository.save(userB);

        ResponseEntity<?> response = restTemplate.exchange("/api/users/" + userB.getId() + "/activation",
                HttpMethod.PATCH, new HttpEntity<>(userEnableRequest, headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.findById(userB.getId()).get().isEnabled()).isTrue();
    }

    @Test
    public void shouldNotEnableUserWhenNonAdminUserIsLoggedIn() {
        UserEnableRequest userEnableRequest = new UserEnableRequest(true);
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), userB);
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<ExceptionResponse> response = restTemplate.exchange("/api/users/" + userA.getId() + "/activation",
                HttpMethod.PATCH, new HttpEntity<>(userEnableRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldNotEnableUserWhenInvalidUserId() {
        UserEnableRequest userEnableRequest = new UserEnableRequest(true);

        ResponseEntity<ExceptionResponse> response = restTemplate.exchange("/api/users/" + 99 + "/activation",
                HttpMethod.PATCH, new HttpEntity<>(userEnableRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("No user found with ID: 99");
    }
}
