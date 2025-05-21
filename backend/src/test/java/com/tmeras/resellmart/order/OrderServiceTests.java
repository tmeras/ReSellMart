package com.tmeras.resellmart.order;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.address.AddressRepository;
import com.tmeras.resellmart.address.AddressResponse;
import com.tmeras.resellmart.cart.CartItem;
import com.tmeras.resellmart.cart.CartItemRepository;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.email.EmailService;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductImage;
import com.tmeras.resellmart.product.ProductResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import com.tmeras.resellmart.user.UserResponse;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {

    private static final String WEBHOOK_SECRET = "secret";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private OrderService orderService;

    private CartItem cartItemA;
    private Address addressA;
    private Address addressB;
    private Order orderA;
    private Order orderB;
    private OrderRequest orderRequestA;
    private OrderResponse orderResponseA;
    private OrderResponse orderResponseB;
    private User userA;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        // Mock external properties for fields annotated with @Value
        ReflectionTestUtils.setField(orderService, "webhookSecret", WEBHOOK_SECRET);

        // Initialise test objects
        Role adminRole = new Role(1, "ADMIN");
        Role userRole = new Role(2, "USER");
        userA = TestDataUtils.createUserA(Set.of(adminRole));
        User userB = TestDataUtils.createUserB(Set.of(userRole));
        UserResponse userResponseA = TestDataUtils.createUserResponseA(Set.of(adminRole));
        UserResponse userResponseB = TestDataUtils.createUserResponseB(Set.of(userRole));

        Category category = TestDataUtils.createCategoryA();
        Product productA = TestDataUtils.createProductA(category, userA);
        Product productB = TestDataUtils.createProductB(category, userB);
        CategoryResponse categoryResponse = TestDataUtils.createCategoryResponseA();
        ProductResponse productResponseA = TestDataUtils.createProductResponseA(categoryResponse, userResponseA);
        ProductResponse productResponseB = TestDataUtils.createProductResponseB(categoryResponse, userResponseB);
        productA.setImages(List.of(new ProductImage(1, "image.jpeg", "image/jpeg", "/path")));
        cartItemA = new CartItem(1, productA, productA.getAvailableQuantity(), userA, ZonedDateTime.now());

        addressA = TestDataUtils.createAddressA(userA);
        addressB = TestDataUtils.createAddressB(userB);
        orderA = TestDataUtils.createOrderA(userA, addressA, productB);
        orderB = TestDataUtils.createOrderB(userB, addressB, productA);
        orderRequestA = TestDataUtils.createOrderRequestA(addressA.getId());
        AddressResponse addressResponseA = TestDataUtils.createAddressResponseA(userA.getId());
        AddressResponse addressResponseB = TestDataUtils.createAddressResponseB(userB.getId());
        orderResponseA = TestDataUtils.createOrderResponseA(addressResponseA, userResponseA, productResponseB);
        orderResponseB = TestDataUtils.createOrderResponseB(addressResponseB, userResponseB, productResponseA);

        authentication = new UsernamePasswordAuthenticationToken(
                userA, userA.getPassword(), userA.getAuthorities()
        );
    }

    @Test
    public void shouldSaveOrderWhenValidRequest(
            @Mock Session session,
            @Captor ArgumentCaptor<SessionCreateParams> sessionParamsCaptor,
            @Captor ArgumentCaptor<Order> orderCaptor
    ) throws MessagingException, IOException, StripeException {
        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(addressA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.of(addressA));
        when(cartItemRepository.findAllWithProductDetailsByUserId(userA.getId()))
                .thenReturn(List.of(cartItemA));
        when(fileService.readFileFromPath(cartItemA.getProduct().getImages().get(0).getImagePath())).thenReturn(new byte[1]);
        when(fileService.saveOrderItemImage(
                any(byte[].class),
                eq(cartItemA.getProduct().getImages().get(0).getName()),
                eq(cartItemA.getProduct().getId()))
        ).thenReturn("path");
        when(orderRepository.save(any(Order.class))).thenReturn(orderA);
        when(session.getUrl()).thenReturn("url");
        when(session.getId()).thenReturn("sessionId");

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(sessionParamsCaptor.capture())).thenReturn(session);

            Map<String, String> result = orderService.save(orderRequestA, authentication);

            assertThat(result.get("redirectUrl")).isEqualTo("url");
            assertThat(result.get("orderId")).isEqualTo(orderA.getId().toString());
            verify(orderRepository).save(orderCaptor.capture());
        }

        SessionCreateParams sessionCreateParams = sessionParamsCaptor.getValue();
        assertThat(sessionCreateParams.getLineItems().get(0).getPriceData().getUnitAmountDecimal())
                .isEqualTo(cartItemA.getProduct().getPrice().multiply(BigDecimal.valueOf(100)));
        assertThat(sessionCreateParams.getLineItems().get(0).getQuantity())
                .isEqualTo(cartItemA.getQuantity().longValue());
        assertThat(sessionCreateParams.getLineItems().get(0).getPriceData().getProductData().getName())
                .isEqualTo(cartItemA.getProduct().getName());

        Order order = orderCaptor.getValue();
        assertThat(order.getPlacedAt()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getBuyer()).isEqualTo(userA);
        assertThat(order.getBillingAddress()).isEqualTo(addressA.getFullAddress());
        assertThat(order.getDeliveryAddress()).isEqualTo(addressA.getFullAddress());
        assertThat(order.getStripeCheckoutId()).isEqualTo("sessionId");

        assertThat(order.getOrderItems().size()).isEqualTo(1);
        OrderItem orderItem = order.getOrderItems().get(0);
        assertThat(orderItem.getStatus()).isEqualTo(OrderItemStatus.PENDING_PAYMENT);
        assertThat(orderItem.getProduct()).isEqualTo(cartItemA.getProduct());
        assertThat(orderItem.getProductQuantity()).isEqualTo(cartItemA.getQuantity());
        assertThat(orderItem.getProductName()).isEqualTo(cartItemA.getProduct().getName());
        assertThat(orderItem.getProductPrice()).isEqualTo(cartItemA.getProduct().getPrice());
        assertThat(orderItem.getProductCondition()).isEqualTo(cartItemA.getProduct().getCondition());
        assertThat(orderItem.getProductImagePath()).isEqualTo("path");
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidBillingAddressId() {
        orderRequestA.setBillingAddressId(99);

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No billing address found with ID: 99");
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidDeliveryAddressId() {
        orderRequestA.setDeliveryAddressId(99);

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(addressA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No delivery address found with ID: 99");
    }

    @Test
    public void shouldNotSaveOrderWhenAddressBelongsToDifferentUser() {
        orderRequestA.setBillingAddressId(addressB.getId());

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.of(addressB));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(addressB));

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("One or both of the specified addresses are related to another user");
    }

    @Test
    public void shouldNotSaveOrderWhenUserCartIsEmpty() {
        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(addressA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.of(addressA));
        when(cartItemRepository.findAllWithProductDetailsByUserId(userA.getId()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("You do not have any items in your cart");
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidCartItemQuantity() {
        cartItemA.setQuantity(99);

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(addressA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.of(addressA));
        when(cartItemRepository.findAllWithProductDetailsByUserId(userA.getId()))
                .thenReturn(List.of(cartItemA));

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("Requested quantity of product with ID '" + cartItemA.getProduct().getId() +
                        "' cannot be larger than available quantity");
    }

    @Test
    public void shouldHandleCheckoutCompletedStripeEventWhenValidRequest(
            @Mock Event event, @Mock Session session, @Mock EventDataObjectDeserializer eventDataObjectDeserializer,
            @Mock PaymentIntent paymentIntent, @Mock com.stripe.model.PaymentMethod paymentMethod
    ) throws StripeException, MessagingException {
        Integer initialQuantity = orderA.getOrderItems().get(0).getProduct().getAvailableQuantity();
        String payload = "payload";
        String sigHeader = "sigHeader";
        orderA.setStatus(OrderStatus.PENDING_PAYMENT);
        orderA.getOrderItems().get(0).setStatus(OrderItemStatus.PENDING_PAYMENT);

        try (MockedStatic<Webhook> webhookStatic = mockStatic(Webhook.class);
             MockedStatic<Session> sessionStatic = mockStatic(Session.class);
             MockedStatic<PaymentIntent> paymentIntentStatic = mockStatic(PaymentIntent.class);
             MockedStatic<com.stripe.model.PaymentMethod> paymentMethodStatic = mockStatic(PaymentMethod.class)
        ) {
            webhookStatic.when(() -> Webhook.constructEvent(payload, sigHeader, WEBHOOK_SECRET)).thenReturn(event);
            when(event.getType()).thenReturn("checkout.session.completed");
            when(event.getDataObjectDeserializer()).thenReturn(eventDataObjectDeserializer);
            when(eventDataObjectDeserializer.getObject()).thenReturn(Optional.of(session));
            when(session.getId()).thenReturn(orderA.getStripeCheckoutId());
            when(orderRepository.findWithProductsAndBuyerDetailsByStripeCheckoutId(orderA.getStripeCheckoutId()))
                    .thenReturn(Optional.of(orderA));
            sessionStatic.when(() -> Session.retrieve(orderA.getStripeCheckoutId())).thenReturn(session);
            when(session.getPaymentIntent()).thenReturn("paymentIntentId");
            paymentIntentStatic.when(() -> PaymentIntent.retrieve("paymentIntentId"))
                    .thenReturn(paymentIntent);
            when(paymentIntent.getPaymentMethod()).thenReturn("paymentMethodId");
            paymentMethodStatic.when(() -> com.stripe.model.PaymentMethod.retrieve("paymentMethodId"))
                    .thenReturn(paymentMethod);
            when(paymentMethod.getType()).thenReturn("cash");

            String response = orderService.handleStripeEvent(payload, sigHeader);

            assertThat(response).isEqualTo("success");
        }

        /*verify(orderRepository, times(1)).save(orderA);
        verify(productRepository, times(1)).save(orderA.getOrderItems().get(0).getProduct());
        verify(cartItemRepository, times(1))
                .deleteAllByUserIdAndProductIdIn(userA.getId(),
                        List.of(orderA.getOrderItems().get(0).getProduct().getId()));
        verify(emailService, times(1))
                .sendPurchaseConfirmationEmail(orderA.getBuyer().getEmail(), orderA);
        verify(emailService, times(1))
                .sendSaleConfirmationEmail(
                        orderA.getOrderItems().get(0).getProductSeller().getEmail(),
                        orderA.getOrderItems().get(0).getProductSeller().getRealName(),
                        orderA,
                        orderA.getOrderItems(),
                        orderA.calculateTotalPrice());*/

        verify(paymentIntent, times(1)).capture();
        assertThat(orderA.getOrderItems().get(0).getProduct().getAvailableQuantity())
                .isEqualTo(initialQuantity - orderA.getOrderItems().get(0).getProductQuantity());
        assertThat(orderA.getOrderItems().get(0).getStatus()).isEqualTo(OrderItemStatus.PENDING_SHIPMENT);
        assertThat(orderA.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(orderA.getPaymentMethod()).isEqualTo("cash");
    }

    @Test
    public void shouldNotFulfillOrderWhenInvalidStripeSessionId() {
        when(orderRepository.findWithProductsAndBuyerDetailsByStripeCheckoutId(orderA.getStripeCheckoutId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.fulfillOrder(orderA.getStripeCheckoutId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No order found with Stripe session ID: " + orderA.getStripeCheckoutId());
    }

    @Test
    public void shouldNotFulfillOrderWhenOrderAlreadyFulfilled() throws StripeException, MessagingException {
        Integer initialQuantity = orderA.getOrderItems().get(0).getProduct().getAvailableQuantity();
        orderA.setStatus(OrderStatus.PAID);

        when(orderRepository.findWithProductsAndBuyerDetailsByStripeCheckoutId(orderA.getStripeCheckoutId()))
                .thenReturn(Optional.of(orderA));

        orderService.fulfillOrder(orderA.getStripeCheckoutId());

        // Assert that product quantity has not been reduced
        assertThat(orderA.getOrderItems().get(0).getProduct().getAvailableQuantity())
                .isEqualTo(initialQuantity);
    }

    @Test
    public void shouldNotFulfillOrderWhenProductIsDeleted(@Mock Session session) throws MessagingException {
        orderA.setStatus(OrderStatus.PENDING_PAYMENT);
        orderA.getOrderItems().get(0).getProduct().setIsDeleted(true);

        when(orderRepository.findWithProductsAndBuyerDetailsByStripeCheckoutId(orderA.getStripeCheckoutId()))
                .thenReturn(Optional.of(orderA));

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve(orderA.getStripeCheckoutId())).thenReturn(session);

            assertThatThrownBy(() -> orderService.fulfillOrder(orderA.getStripeCheckoutId()))
                    .isInstanceOf(APIException.class)
                    .hasMessage("Product with ID '" + orderA.getOrderItems().get(0).getProduct().getId() +
                            "' does not have the required stock");

            verify(emailService, times(1))
                    .sendOrderCancellationEmail(
                            orderA.getBuyer().getEmail(),
                            orderA,
                            orderA.getOrderItems().get(0).getProduct(),
                            orderA.getOrderItems().get(0).getProductQuantity()
                    );
        }
    }

    @Test
    public void shouldFindAllOrders() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_ORDERS_BY).ascending() : Sort.by(AppConstants.SORT_ORDERS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Order> page = new PageImpl<>(List.of(orderA, orderB));

        when(orderRepository.findAll(pageable)).thenReturn(page);
        when(orderMapper.toOrderResponse(orderA)).thenReturn(orderResponseA);
        when(orderMapper.toOrderResponse(orderB)).thenReturn(orderResponseB);

        PageResponse<OrderResponse> pageResponse =
                orderService.findAll(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_ORDERS_BY, AppConstants.SORT_DIR);

        assertThat(pageResponse.getContent()).isNotNull();
        assertThat(pageResponse.getTotalElements()).isEqualTo(2);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(orderResponseA);
        assertThat(pageResponse.getContent().get(1)).isEqualTo(orderResponseB);
    }

    @Test
    public void shouldFindAllOrdersByBuyerIdWhenValidRequest() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_ORDERS_BY).ascending() : Sort.by(AppConstants.SORT_ORDERS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Order> page = new PageImpl<>(List.of(orderA));

        when(orderRepository.findAllPaidByBuyerId(pageable, userA.getId())).thenReturn(page);
        when(orderMapper.toOrderResponse(orderA)).thenReturn(orderResponseA);

        PageResponse<OrderResponse> pageResponse =
                orderService.findAllByBuyerId(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_ORDERS_BY, AppConstants.SORT_DIR, userA.getId(), authentication);

        assertThat(pageResponse.getContent()).isNotNull();
        assertThat(pageResponse.getTotalElements()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(orderResponseA);
    }

    @Test
    public void shouldNotFindAllOrdersByBuyerIdWhenBuyerIsNotLoggedIn() {
        assertThatThrownBy(
                () -> orderService.findAllByBuyerId(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_ORDERS_BY, AppConstants.SORT_DIR, orderB.getBuyer().getId(), authentication)
        ).isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to view the orders of this user");
    }

    @Test
    public void shouldFindAllOrdersByProductSellerIdWhenValidRequest() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_ORDERS_BY).ascending() : Sort.by(AppConstants.SORT_ORDERS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Order> page = new PageImpl<>(List.of(orderB));

        when(orderRepository.findAllPaidByProductSellerId(pageable, userA.getId())).thenReturn(page);
        when(orderMapper.toOrderResponse(orderB)).thenReturn(orderResponseB);

        PageResponse<OrderResponse> pageResponse =
                orderService.findAllByProductSellerId(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_ORDERS_BY, AppConstants.SORT_DIR, userA.getId(), authentication);

        assertThat(pageResponse.getContent()).isNotNull();
        assertThat(pageResponse.getTotalElements()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(orderResponseB);
    }

    @Test
    public void shouldNotFindAllOrdersByProductSellerIdWhenSellerIsNotLoggedIn() {
        assertThatThrownBy(
                () -> orderService.findAllByProductSellerId(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_ORDERS_BY, AppConstants.SORT_DIR, orderA.getOrderItems().get(0).getProductSeller().getId(), authentication)
        ).isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to view these orders");
    }
}
