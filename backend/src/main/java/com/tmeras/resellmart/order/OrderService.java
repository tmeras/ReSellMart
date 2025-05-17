package com.tmeras.resellmart.order;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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

    public OrderResponse save(OrderRequest orderRequest, Authentication authentication) throws MessagingException, IOException {
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
        List<Product> cartProducts = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();
        if (cartItems.isEmpty())
            throw new APIException("You do not have any items in your cart");

        // TODO: Stripe integration + emails on placed to seller, on delivered to buyer

        for (CartItem cartItem : cartItems) {
            Product cartProduct = cartItem.getProduct();
            if (cartProduct.getAvailableQuantity() < cartItem.getQuantity())
                throw new APIException("Requested quantity of product with ID '" + cartProduct.getId() +
                        "' cannot be larger than available quantity");
            if (cartProduct.getIsDeleted())
                throw new APIException("Product with ID '" + cartProduct.getId() +
                        "' is no longer available for sale");

            // Reduce product's available quantity by the requested quantity
            cartProduct.setAvailableQuantity(cartProduct.getAvailableQuantity() - cartItem.getQuantity());
            cartProducts.add(cartProduct);

            // Create corresponding order item
            OrderItem orderItem = new OrderItem();
            orderItem.setStatus(OrderItemStatus.PENDING_PAYMENT);
            orderItem.setProductId(cartProduct.getId());
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

        // Empty user's cart and update available quantity of products
        cartItemRepository.deleteAll(cartItems);
        productRepository.saveAll(cartProducts);

        // Save order
        Order order = new Order();
        order.setPlacedAt(ZonedDateTime.now());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setBuyer(currentUser);
        order.setBillingAddress(billingAddress.getFullAddress());
        order.setDeliveryAddress(deliveryAddress.getFullAddress());
        order.setOrderItems(orderItems);
        order = orderRepository.save(order);

        // TODO: Update thymeleaf template
        // Send order confirmation mail
        //emailService.sendOrderConfirmationEmail(currentUser.getEmail(), order);

        return orderMapper.toOrderResponse(order);
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
