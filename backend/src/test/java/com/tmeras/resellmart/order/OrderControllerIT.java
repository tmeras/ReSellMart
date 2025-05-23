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
import com.tmeras.resellmart.product.ProductImage;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(MockitoExtension.class)
public class OrderControllerIT {

    private static final ClassPathResource TEST_IMAGE_1 = new ClassPathResource("test_image_1.jpeg");

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
    private Address addressA;
    private Address addressB;

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
        Product productA = TestDataUtils.createProductA(category, userA);
        productA.setId(null);
        productA = productRepository.save(productA);
        Product productB = TestDataUtils.createProductB(category, userB);
        productB.setId(null);
        productB = productRepository.save(productB);

        addressA = TestDataUtils.createAddressA(userA);
        addressA.setId(null);
        addressA = addressRepository.save(addressA);
        addressB = TestDataUtils.createAddressB(userB);
        addressB.setId(null);
        addressB = addressRepository.save(addressB);

        orderA = TestDataUtils.createOrderA(userA, addressA, productB);
        orderA.setId(null);
        orderA.getOrderItems().get(0).setId(null);
        orderA = orderRepository.save(orderA);
        orderB = TestDataUtils.createOrderB(userB, addressB, productA);
        orderB.setId(null);
        orderB.getOrderItems().get(0).setId(null);
        orderB = orderRepository.save(orderB);

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
    public void shouldSaveOrderWhenValidRequest() throws IOException {
        // Add a product to the user's cart before placing order
        File testFile = TEST_IMAGE_1.getFile();
        ProductImage productImage = ProductImage.builder()
                .name(testFile.getName())
                .type("image/jpeg")
                .imagePath(testFile.getAbsolutePath())
                .build();
        Product orderProduct = Product.builder()
                .name("Test Product")
                .description("Test description")
                .price(BigDecimal.valueOf(10))
                .condition(ProductCondition.NEW)
                .availableQuantity(5)
                .isDeleted(false)
                .category(orderB.getOrderItems().get(0).getProduct().getCategory())
                .seller(orderB.getBuyer())
                .images(List.of(productImage))
                .build();
        orderProduct = productRepository.save(orderProduct);

        CartItem cartItem = CartItem.builder()
                .product(orderProduct)
                .quantity(2)
                .user(orderA.getBuyer())
                .build();
        cartItem = cartItemRepository.save(cartItem);

        OrderRequest orderRequest = OrderRequest.builder()
                .billingAddressId(addressA.getId())
                .deliveryAddressId(addressA.getId())
                .build();

        ResponseEntity<Map<String, String>> response =
                restTemplate.exchange("/api/orders", HttpMethod.POST,
                        new HttpEntity<>(orderRequest, headers), new ParameterizedTypeReference<>() {
                        });

        System.out.println(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("redirectUrl")).isNotBlank();
        assertThat(response.getBody().get("orderId")).isNotBlank();

        Integer orderId = Integer.valueOf(response.getBody().get("orderId"));
        Optional<Order> createdOrder = orderRepository.findWithProductsAndBuyerDetailsById(orderId);
        assertThat(createdOrder).isPresent();
        assertThat(createdOrder.get().getPlacedAt()).isNotNull();
        assertThat(createdOrder.get().getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(createdOrder.get().getStripeCheckoutId()).isNotBlank();
        assertThat(createdOrder.get().getBillingAddress()).isNotBlank();
        assertThat(createdOrder.get().getDeliveryAddress()).isNotBlank();
        assertThat(createdOrder.get().getBuyer().getId()).isEqualTo(cartItem.getUser().getId());

        assertThat(createdOrder.get().getOrderItems().size()).isEqualTo(1);
        OrderItem orderItem = createdOrder.get().getOrderItems().get(0);
        assertThat(orderItem.getStatus()).isEqualTo(OrderItemStatus.PENDING_PAYMENT);
        assertThat(orderItem.getProduct().getId()).isEqualTo(orderProduct.getId());
        assertThat(orderItem.getProductQuantity()).isEqualTo(cartItem.getQuantity());
        assertThat(orderItem.getProductName()).isEqualTo(orderProduct.getName());
        assertThat(orderItem.getProductPrice().compareTo(orderProduct.getPrice())).isEqualTo(0);
        assertThat(orderItem.getProductCondition()).isEqualTo(orderProduct.getCondition());
        assertThat(orderItem.getProductImagePath()).isNotBlank();
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidRequest() {
        OrderRequest orderRequest = OrderRequest.builder()
                .billingAddressId(addressA.getId())
                .deliveryAddressId(null)
                .build();
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("deliveryAddressId", "Delivery address ID must not be empty");

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
                .billingAddressId(99)
                .deliveryAddressId(addressA.getId())
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
                .billingAddressId(addressA.getId())
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
                .billingAddressId(addressB.getId())
                .deliveryAddressId(addressA.getId())
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
                .billingAddressId(addressA.getId())
                .deliveryAddressId(addressA.getId())
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
                .condition(ProductCondition.NEW)
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
                .billingAddressId(addressA.getId())
                .deliveryAddressId(addressA.getId())
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
    public void shouldNotSaveOrderWhenCartItemIsDeleted() {
        // Add a product to the user's cart before placing order
        Product orderProduct = Product.builder()
                .name("Test Product")
                .description("Test description")
                .price(BigDecimal.valueOf(10.0))
                .condition(ProductCondition.NEW)
                .availableQuantity(5)
                .isDeleted(true)
                .category(orderB.getOrderItems().get(0).getProduct().getCategory())
                .seller(orderB.getBuyer())
                .build();
        orderProduct = productRepository.save(orderProduct);

        CartItem cartItem = CartItem.builder()
                .product(orderProduct)
                .quantity(2)
                .user(orderA.getBuyer())
                .build();
        cartItemRepository.save(cartItem);

        OrderRequest orderRequest = OrderRequest.builder()
                .billingAddressId(addressA.getId())
                .deliveryAddressId(addressA.getId())
                .build();

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders", HttpMethod.POST,
                        new HttpEntity<>(orderRequest, headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Product with ID '" + orderProduct.getId() +
                        "' is no longer available for sale");
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
                restTemplate.exchange("/api/users/" + orderA.getBuyer().getId() + "/purchases", HttpMethod.GET,
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
                restTemplate.exchange("/api/users/" + orderB.getBuyer().getId() + "/purchases", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to view the orders of this user");
    }

    @Test
    public void shouldFindAllOrderByProductSellerIdWhenValidRequest() {
        ResponseEntity<PageResponse<OrderResponse>> response =
                restTemplate.exchange("/api/users/" + orderB.getOrderItems().get(0).getProductSeller().getId() + "/sales", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(orderB.getId());
    }

    @Test
    public void shouldNotFindAllOrderByProductSellerIdWhenSellerIsNotLoggedIn() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/users/" + orderA.getOrderItems().get(0).getProductSeller().getId() + "/sales", HttpMethod.GET,
                        new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to view these orders");
    }

    @Test
    public void shouldMarkOrderItemAsShippedWhenValidRequest() {
        ResponseEntity<?> response =
                restTemplate.exchange("/api/orders/" + orderB.getId() + "/products/" +
                                orderB.getOrderItems().get(0).getProduct().getId() + "/ship",
                        HttpMethod.PATCH, new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Optional<Order> updatedOrder = orderRepository.findWithProductsAndBuyerDetailsById(orderB.getId());
        assertThat(updatedOrder).isPresent();
        assertThat(updatedOrder.get().getOrderItems().get(0).getStatus())
                .isEqualTo(OrderItemStatus.SHIPPED);
    }

    @Test
    public void shouldNotMarkOrderItemAsShippedWhenInvalidOrderId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/99/products/" +
                                orderB.getOrderItems().get(0).getProduct().getId() + "/ship",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("No order found with ID: 99");
    }

    @Test
    public void shouldNotMarkOrderItemAsShippedWhenOrderIsNotPaid() {
        orderB.setStatus(OrderStatus.PENDING_PAYMENT);
        orderRepository.save(orderB);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/" + orderB.getId() + "/products/" +
                                orderB.getOrderItems().get(0).getProduct().getId() + "/ship",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Order with ID: " + orderB.getId() + " has not been paid yet");
    }

    @Test
    public void shouldNotMarkOrderItemAsShippedWhenInvalidProductId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/" + orderB.getId() + "/products/99/ship",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("The order does not include a product with ID: 99");
    }

    @Test
    public void shouldNotMarkOrderItemAsShippedWhenProductSellerIsNotLoggedIn() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/" + orderA.getId() + "/products/" +
                                orderA.getOrderItems().get(0).getProduct().getId() + "/ship",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to mark this product as shipped");
    }

    @Test
    public void shouldMarkOrderItemAsDeliveredWhenValidRequest() {
        orderA.getOrderItems().get(0).setStatus(OrderItemStatus.SHIPPED);
        orderRepository.save(orderA);

        ResponseEntity<?> response =
                restTemplate.exchange("/api/orders/" + orderA.getId() + "/products/" +
                                orderA.getOrderItems().get(0).getProduct().getId() + "/deliver",
                        HttpMethod.PATCH, new HttpEntity<>(headers), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Optional<Order> updatedOrder = orderRepository.findWithProductsAndBuyerDetailsById(orderA.getId());
        assertThat(updatedOrder).isPresent();
        assertThat(updatedOrder.get().getOrderItems().get(0).getStatus())
                .isEqualTo(OrderItemStatus.DELIVERED);
    }

    @Test
    public void shouldNotMarkOrderItemAsDeliveredWhenInvalidOrderId() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/99/products/" +
                                orderA.getOrderItems().get(0).getProduct().getId() + "/deliver",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("No order found with ID: 99");
    }

    @Test
    public void shouldNotMarkOrderItemAsDeliveredWhenOrderIsNotPaid() {
        orderA.setStatus(OrderStatus.PENDING_PAYMENT);
        orderRepository.save(orderA);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/" + orderA.getId() + "/products/" +
                                orderA.getOrderItems().get(0).getProduct().getId() + "/deliver",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Order with ID: " + orderA.getId() + " has not been paid yet");
    }

    @Test
    public void shouldNotMarkOrderItemAsDeliveredWhenInvalidProductId() {
        orderA.getOrderItems().get(0).setStatus(OrderItemStatus.SHIPPED);
        orderRepository.save(orderA);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/" + orderA.getId() + "/products/99/deliver",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("The order does not include a product with ID: 99");
    }

    @Test
    public void shouldNotMarkOrderItemAsDeliveredWhenOrderItemIsNotShipped() {
        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/" + orderA.getId() + "/products/" +
                                orderA.getOrderItems().get(0).getProduct().getId() + "/deliver",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Product with ID: " + orderA.getOrderItems().get(0).getProduct().getId() +
                        " has not been shipped yet");
    }

    @Test
    public void shouldNotMarkOrderItemAsDeliveredWhenBuyerIsNotLoggedIn() {
        orderB.getOrderItems().get(0).setStatus(OrderItemStatus.SHIPPED);
        orderRepository.save(orderB);

        ResponseEntity<ExceptionResponse> response =
                restTemplate.exchange("/api/orders/" + orderB.getId() + "/products/" +
                                orderB.getOrderItems().get(0).getProduct().getId() + "/deliver",
                        HttpMethod.PATCH, new HttpEntity<>(headers), ExceptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("You do not have permission to mark this product as delivered");
    }
}
