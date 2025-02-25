package com.tmeras.resellmart.order;

import com.tmeras.resellmart.address.AddressMapper;
import com.tmeras.resellmart.product.ProductMapper;
import com.tmeras.resellmart.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderMapper {

    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final UserMapper userMapper;

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> orderItemResponses = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItemResponses.add(OrderItemResponse.builder()
                    .id(orderItem.getId())
                    .product(productMapper.toProductResponse(orderItem.getProduct()))
                    .productQuantity(orderItem.getProductQuantity())
                    .build()
            );
        }

        return OrderResponse.builder()
                .id(order.getId())
                .placedAt(order.getPlacedAt())
                .paymentMethod(order.getPaymentMethod())
                .billingAddress(addressMapper.toAddressResponse(order.getBillingAddress()))
                .deliveryAddress(addressMapper.toAddressResponse(order.getDeliveryAddress()))
                .buyer(userMapper.toUserResponse(order.getBuyer()))
                .orderItems(orderItemResponses)
                .build();
    }
}
