package com.tmeras.resellmart.order;

import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderMapper {

    private final UserMapper userMapper;

    private final FileService fileService;

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> orderItemResponses = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItemResponses.add(toOrderItemResponse(orderItem));
        }

        return OrderResponse.builder()
                .id(order.getId())
                .placedAt(order.getPlacedAt())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .stripeCheckoutId(order.getStripeCheckoutId())
                .billingAddress(order.getBillingAddress())
                .deliveryAddress(order.getDeliveryAddress())
                .total(order.calculateTotalPrice())
                .buyer(userMapper.toUserResponse(order.getBuyer()))
                .orderItems(orderItemResponses)
                .build();
    }

    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        byte[] image = orderItem.getProductImagePath() != null ?
                fileService.readFileFromPath(orderItem.getProductImagePath()) : null;

        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .status(orderItem.getStatus())
                .productId(orderItem.getProduct().getId())
                .productQuantity(orderItem.getProductQuantity())
                .productName(orderItem.getProductName())
                .productPrice(orderItem.getProductPrice())
                .productCondition(orderItem.getProductCondition())
                .productImage(image)
                .productSeller(userMapper.toUserResponse(orderItem.getProductSeller()))
                .build();
    }
}
