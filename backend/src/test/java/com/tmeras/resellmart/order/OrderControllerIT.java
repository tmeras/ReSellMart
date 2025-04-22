package com.tmeras.resellmart.order;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.address.AddressRepository;
import com.tmeras.resellmart.cart.CartItem;
import com.tmeras.resellmart.cart.CartItemRepository;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.ExceptionResponse;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductCondition;
import com.tmeras.resellmart.product.ProductRepository;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class OrderControllerIT {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final TestRestTemplate restTemplate;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AddressRepository addressRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    // Used to add JWT in requests
    private HttpHeaders headers;

    private Order orderA;
    private Order orderB;

    @Autowired
    public OrderControllerIT(
            TestRestTemplate restTemplate, UserRepository userRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, AddressRepository addressRepository,
            CartItemRepository cartItemRepository, ProductRepository productRepository,
            CategoryRepository categoryRepository, OrderRepository orderRepository
    ) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.addressRepository = addressRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    @BeforeEach
    public void setUp() {
        // Empty relevant database tables
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        addressRepository.deleteAll();
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

        Category category = categoryRepository.save(Category.builder().name("category").build());
        Product productA = TestDataUtils.createProductA(category, userB);
        productA.setId(null);
        productA = productRepository.save(productA);
        Product productB = TestDataUtils.createProductB(category, userA);
        productB.setId(null);
        productB = productRepository.save(productB);

        Address addressA = TestDataUtils.createAddressA(userA);
        addressA.setId(null);
        addressA = addressRepository.save(addressA);
        Address addressB = TestDataUtils.createAddressB(userB);
        addressB.setId(null);
        addressB = addressRepository.save(addressB);

        orderA = TestDataUtils.createOrderA(userA, addressA, productA);
        orderA.setId(null);
        orderA.getOrderItems().get(0).setId(null);
        orderA = orderRepository.save(orderA);
        orderB = TestDataUtils.createOrderB(userB, addressB, productB);
        orderB.setId(null);
        orderB.getOrderItems().get(0).setId(null);
        orderB = orderRepository.save(orderB);

        // Generate test JWT with admin user details to include in each authenticated request
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), userA);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testJwt);
    }

    @Test
    public void shouldSaveOrderWhenValidRequest() {
        // Add a product to the user's cart before placing order
        Product orderProduct = Product.builder()
                .name("Test Product")
                .description("Test description")
                .price(BigDecimal.valueOf(10))
                .productCondition(ProductCondition.NEW)
                .availableQuantity(5)
                .isDeleted(false)
                .category(orderB.getOrderItems().get(0).getProduct().getCategory())
                .seller(orderB.getBuyer())
                .build();
        orderProduct = productRepository.save(orderProduct);

        CartItem cartItem = CartItem.builder()
                .product(orderProduct)
                .quantity(2)
                .user(orderA.getBuyer())
                .build();
        cartItem = cartItemRepository.save(cartItem);

        OrderRequest orderRequest = OrderRequest.builder()
                .paymentMethod("CASH")
                .billingAddressId(orderA.getBillingAddress().getId())
                .deliveryAddressId(orderA.getDeliveryAddress().getId())
                .build();

        ResponseEntity<OrderResponse> response =
                restTemplate.exchange("/api/orders", HttpMethod.POST,
                        new HttpEntity<>(orderRequest, headers), OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(response.getBody().getBillingAddress().getId()).isEqualTo(orderRequest.getBillingAddressId());
        assertThat(response.getBody().getDeliveryAddress().getId()).isEqualTo(orderRequest.getDeliveryAddressId());
        assertThat(response.getBody().getBuyer().getId()).isEqualTo(cartItem.getUser().getId());
        assertThat(response.getBody().getOrderItems().size()).isEqualTo(1);
        assertThat(response.getBody().getOrderItems().get(0).getProduct().getId()).isEqualTo(orderProduct.getId());
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidRequest() {
        OrderRequest orderRequest = OrderRequest.builder()
                .paymentMethod(null)
                .billingAddressId(orderA.getBillingAddress().getId())
                .deliveryAddressId(orderA.getDeliveryAddress().getId())
                .build();
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("paymentMethod", "Payment method must not be empty");

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange("/api/orders", HttpMethod.POST,
                        new HttpEntity<>(orderRequest, headers), new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(expectedErrors);
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidBillingAddressId() {
        OrderRequest orderRequest = OrderRequest.builder()
                .paymentMethod("CASH")
                .billingAddressId(99)
                .deliveryAddressId(orderA.getDeliveryAddress().getId())
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders", HttpMethod.POST,
                        new HttpEntity<>(orderRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("No billing address found with ID: 99");
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidDeliveryAddressId() {
        OrderRequest orderRequest = OrderRequest.builder()
                .paymentMethod("CASH")
                .billingAddressId(orderA.getBillingAddress().getId())
                .deliveryAddressId(99)
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders", HttpMethod.POST,
                        new HttpEntity<>(orderRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("No delivery address found with ID: 99");
    }

    @Test
    public void shouldNotSaveOrderWhenAddressBelongsToDifferentUser() {
        OrderRequest orderRequest = OrderRequest.builder()
                .paymentMethod("CASH")
                .billingAddressId(orderB.getBillingAddress().getId())
                .deliveryAddressId(orderA.getDeliveryAddress().getId())
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders", HttpMethod.POST,
                        new HttpEntity<>(orderRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("One or both of the specified addresses are related to another user");
    }

    @Test
    public void shouldNotSaveOrderWhenUserCartIsEmpty() {
        OrderRequest orderRequest = OrderRequest.builder()
                .paymentMethod("CASH")
                .billingAddressId(orderA.getBillingAddress().getId())
                .deliveryAddressId(orderA.getDeliveryAddress().getId())
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders", HttpMethod.POST,
                        new HttpEntity<>(orderRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("You do not have any items in your cart");
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidCartItemQuantity() {
        // Add a product to the user's cart before placing order
        Product orderProduct = Product.builder()
                .name("Test Product")
                .description("Test description")
                .price(BigDecimal.valueOf(10.0))
                .productCondition(ProductCondition.NEW)
                .availableQuantity(5)
                .isDeleted(false)
                .category(orderB.getOrderItems().get(0).getProduct().getCategory())
                .seller(orderB.getBuyer())
                .build();
        orderProduct = productRepository.save(orderProduct);

        CartItem cartItem = CartItem.builder()
                .product(orderProduct)
                .quantity(99)
                .user(orderA.getBuyer())
                .build();
        cartItemRepository.save(cartItem);

        OrderRequest orderRequest = OrderRequest.builder()
                .paymentMethod("CASH")
                .billingAddressId(orderA.getBillingAddress().getId())
                .deliveryAddressId(orderA.getDeliveryAddress().getId())
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders", HttpMethod.POST,
                        new HttpEntity<>(orderRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Requested quantity of product with ID '" + orderProduct.getId() +
                        "' cannot be larger than available quantity");
    }

    @Test
    public void shouldFindAllOrdersWhenValidRequest() {
        ResponseEntity<PageResponse<OrderResponse>> response =
                restTemplate.exchange("/api/orders", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(orderA.getId());
        assertThat(response.getBody().getContent().get(1).getId()).isEqualTo(orderB.getId());
    }

    @Test
    public void shouldNotFindAllOrdersWhenNonAdminUser() {
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), orderB.getBuyer());
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders", HttpMethod.GET,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldFindAllOrdersByBuyerIdWhenValidRequest() {
        ResponseEntity<PageResponse<OrderResponse>> response =
                restTemplate.exchange("/api/users/" + orderA.getBuyer().getId() + "/orders", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(orderA.getId());
    }

    @Test
    public void shouldNotFindAllOrdersByBuyerIdWhenBuyerIsNotLoggedIn() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + orderB.getBuyer().getId() + "/orders", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to view the orders of this user");
    }

    @Test
    public void shouldDeleteOrderWhenValidRequest() {
        ResponseEntity<?> response =
                restTemplate.exchange("/api/orders/" + orderA.getId(), HttpMethod.DELETE,
                        new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(orderRepository.findById(orderA.getId())).isEmpty();
    }

    @Test
    public void shouldNotDeleteOrderWhenNonAdminUser() {
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), orderB.getBuyer());
        headers.set("Authorization", "Bearer " + testJwt);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/" + orderA.getId(), HttpMethod.DELETE,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
