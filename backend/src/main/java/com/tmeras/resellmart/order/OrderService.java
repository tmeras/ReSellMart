package com.tmeras.resellmart.order;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.address.AddressRepository;
import com.tmeras.resellmart.cart.CartItem;
import com.tmeras.resellmart.cart.CartItemRepository;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.email.EmailService;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final EmailService emailService;
    private final FileService fileService;
    private final AsyncFulfillmentService asyncFulfillmentService;

    @Value("${application.backend.base-https-url}")
    private String backendBaseHttpsUrl;

    @Value("${application.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${application.stripe.checkout-success-url}")
    private String checkoutSuccessUrl;

    @Value("${application.stripe.checkout-cancel-url}")
    private String checkoutCancelUrl;

    @Value("${application.stripe.webhook-secret}")
    private String webhookSecret;

    public String save(OrderRequest orderRequest, Authentication authentication) throws MessagingException, IOException, StripeException {
        User currentUser = (User) authentication.getPrincipal();

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findWithAssociationsById(currentUser.getId()).get();

        Address billingAddress = addressRepository.findWithAssociationsById(orderRequest.getBillingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("No billing address found with ID: " + orderRequest.getBillingAddressId()));
        Address deliveryAddress = addressRepository.findWithAssociationsById(orderRequest.getDeliveryAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("No delivery address found with ID: " + orderRequest.getDeliveryAddressId()));
        if (!billingAddress.getUser().getId().equals(currentUser.getId()) ||
                !deliveryAddress.getUser().getId().equals(currentUser.getId()))
            throw new APIException("One or both of the specified addresses are related to another user");

        // Fetch the user's cart items and create corresponding order items
        List<CartItem> cartItems = cartItemRepository.findAllWithProductDetailsByUserId(currentUser.getId());
        List<OrderItem> orderItems = new ArrayList<>();
        if (cartItems.isEmpty())
            throw new APIException("You do not have any items in your cart");

        // Build Stripe checkout session parameters
        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendBaseUrl + checkoutSuccessUrl + "?sessionId={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendBaseUrl + checkoutCancelUrl)
                .setCustomerEmail(currentUser.getEmail())
                // Capture funds later after order fulfilment
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .setCaptureMethod(SessionCreateParams.PaymentIntentData.CaptureMethod.MANUAL)
                                .build()
                );

        for (CartItem cartItem : cartItems) {
            Product cartProduct = cartItem.getProduct();
            if (cartProduct.getAvailableQuantity() < cartItem.getQuantity())
                throw new APIException("Requested quantity of product with ID '" + cartProduct.getId() +
                        "' cannot be larger than available quantity");
            if (cartProduct.getIsDeleted())
                throw new APIException("Product with ID '" + cartProduct.getId() +
                        "' is no longer available for sale");

            // Add product to Stripe checkout session
            sessionBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(cartItem.getQuantity().longValue())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("gbp")
                                            .setUnitAmountDecimal(cartProduct.getPrice().multiply(BigDecimal.valueOf(100)))
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(cartProduct.getName())
                                                            .addImage(backendBaseHttpsUrl + "/api/products/" + cartProduct.getId()
                                                                    + "/images/primary")
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );

            // Create corresponding order item
            OrderItem orderItem = new OrderItem();
            orderItem.setStatus(OrderItemStatus.PENDING_PAYMENT);
            orderItem.setProduct(cartProduct);
            orderItem.setProductQuantity(cartItem.getQuantity());
            orderItem.setProductName(cartProduct.getName());
            orderItem.setProductPrice(cartProduct.getPrice());
            orderItem.setProductCondition(cartProduct.getCondition());
            orderItem.setProductSeller(cartProduct.getSeller());
            byte[] primaryProductImageBytes = fileService.readFileFromPath(cartProduct.getImages().get(0).getImagePath());
            String primaryProductImageName = cartProduct.getImages().get(0).getName();
            orderItem.setProductImagePath(
                    fileService.saveOrderItemImage(primaryProductImageBytes, primaryProductImageName, cartProduct.getId())
            );
            orderItems.add(orderItem);
        }

        // Finalise checkout session
        SessionCreateParams sessionParams = sessionBuilder.build();
        Session session = Session.create(sessionParams);

        // Save order
        Order order = new Order();
        order.setPlacedAt(ZonedDateTime.now());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setBuyer(currentUser);
        order.setBillingAddress(billingAddress.getFullAddress());
        order.setDeliveryAddress(deliveryAddress.getFullAddress());
        order.setStripeCheckoutId(session.getId());
        order.setOrderItems(orderItems);
        orderRepository.save(order);

        // Return the URL to the Stripe checkout page
        return session.getUrl();
    }

    public String handleStripeEvent(String payload, String sigHeader) throws StripeException, MessagingException {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new APIException("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session sessionEvent = (Session) event.getDataObjectDeserializer().getObject().get();
            fulfillOrder(sessionEvent.getId());
        }
        return "Ok";
    }

    public void fulfillOrder(String sessionId) throws StripeException, MessagingException {
        System.out.println("Fulfilling order for checkout session ID: " + sessionId);
        Order order = orderRepository.findWithProductsAndBuyerDetailsByStripeCheckoutId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No order found with Stripe session ID: " + sessionId));

        // Ensure checkout hasn't already been fulfilled for this session
        if (order.getStatus() == OrderStatus.PAID) {
            System.out.println("Order has already been fulfilled for Stripe session ID: " + sessionId);
            return;
        }

        // Retrieve stripe checkout session
        Session checkoutSession = Session.retrieve(sessionId);

        List<Product> orderProducts = new ArrayList<>();

        // Update availability of products that were ordered
        for (OrderItem orderItem : order.getOrderItems()) {
            Product orderProduct = orderItem.getProduct();

            if ((orderProduct.getAvailableQuantity() < orderItem.getProductQuantity())
                    || orderProduct.getIsDeleted()
            ) {
                emailService.sendOrderCancellationEmail(
                        order.getBuyer().getEmail(),
                        order,
                        orderProduct,
                        orderItem.getProductQuantity()
                );

                throw new APIException("Product with ID '" + orderProduct.getId() +
                        "' does not have the required stock");
            }

            orderProduct.setAvailableQuantity(orderProduct.getAvailableQuantity() - orderItem.getProductQuantity());
            orderProducts.add(orderProduct);
            orderItem.setStatus(OrderItemStatus.PENDING_SHIPMENT);
        }
        order.setStatus(OrderStatus.PAID);

        // Capture funds that were previously authorised
        String paymentIntendId = checkoutSession.getPaymentIntent();
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntendId);
        paymentIntent.capture();

        // Save payment method
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentIntent.getPaymentMethod());
        order.setPaymentMethod(paymentMethod.getType());

        // Run remaining logic asynchronously to quickly return 200 OK to Stripe
        asyncFulfillmentService.finaliseOrder(order, orderProducts);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<OrderResponse> findAll(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Order> orders = orderRepository.findAll(pageable);
        // Initialise lazy associations
        for (Order order : orders) {
            order.getOrderItems().size();
            order.getBuyer().getRoles().size();
            for (OrderItem orderItem : order.getOrderItems()) {
                orderItem.getProductSeller().getRoles().size();
            }
        }
        List<OrderResponse> orderResponses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();

        return new PageResponse<>(
                orderResponses,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isFirst(),
                orders.isLast()
        );
    }

    public PageResponse<OrderResponse> findAllByBuyerId(
            Integer pageNumber, Integer pageSize, String sortBy,
            String sortDirection, Integer buyerId, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(buyerId))
            throw new OperationNotPermittedException("You do not have permission to view the orders of this user");

        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Order> orders = orderRepository.findAllByBuyerId(pageable, buyerId);
        // Initialise lazy associations
        for (Order order : orders) {
            order.getOrderItems().size();
            order.getBuyer().getRoles().size();
            for (OrderItem orderItem : order.getOrderItems()) {
                orderItem.getProductSeller().getRoles().size();
            }
        }
        List<OrderResponse> orderResponses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();

        return new PageResponse<>(
                orderResponses,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isFirst(),
                orders.isLast()
        );
    }

    // Find all the orders that include products sold by the given seller
    public PageResponse<OrderResponse> findAllByProductSellerId(
            Integer pageNumber, Integer pageSize, String sortBy,
            String sortDirection, Integer productSellerId, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(productSellerId))
            throw new OperationNotPermittedException("You do not have permission to view these orders");

        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Order> orders = orderRepository.findAllByProductSellerId(pageable, productSellerId);
        // Initialise lazy associations
        for (Order order : orders) {
            order.getOrderItems().size();
            order.getBuyer().getRoles().size();
            for (OrderItem orderItem : order.getOrderItems()) {
                orderItem.getProductSeller().getRoles().size();
            }
        }
        List<OrderResponse> orderResponses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();

        // Only return order items that are sold by the given seller
        orderResponses.forEach(orderResponse -> {
                    orderResponse.setOrderItems(orderResponse.getOrderItems().stream()
                            .filter(orderItem -> orderItem.getProductSeller().getId().equals(productSellerId))
                            .toList()
                    );
                    orderResponse.setTotal(orderResponse.calculateTotalPrice());
                }
        );

        return new PageResponse<>(
                orderResponses,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isFirst(),
                orders.isLast()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Integer orderId) {
        // TODO: Delete image if not inserted during Flyway migration

        orderRepository.deleteById(orderId);
    }
}
