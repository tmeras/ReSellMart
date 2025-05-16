package com.tmeras.resellmart.order;

import com.tmeras.resellmart.user.UserResponse;
import lombok.*;

import java.math.BigDecimal;
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

    private PaymentMethod paymentMethod;

    private OrderStatus status;

    private String stripeCheckoutId;

    private String billingAddress;

    private String deliveryAddress;

    private BigDecimal total;

    private UserResponse buyer;

    private List<OrderItemResponse> orderItems;
}
