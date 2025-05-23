package com.tmeras.resellmart.order;

import com.tmeras.resellmart.user.UserResponse;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private Integer id;

    private ZonedDateTime placedAt;

    private String paymentMethod;

    private OrderStatus status;

    private String stripeCheckoutId;

    private String billingAddress;

    private String deliveryAddress;

    private BigDecimal total;

    private UserResponse buyer;

    private List<OrderItemResponse> orderItems;

    public BigDecimal calculateTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemResponse orderItem : orderItems) {
            BigDecimal productPrice = orderItem.getProductPrice();
            Integer productQuantity = orderItem.getProductQuantity();
            totalPrice = totalPrice.add(productPrice.multiply(BigDecimal.valueOf(productQuantity)));
        }

        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }
}
