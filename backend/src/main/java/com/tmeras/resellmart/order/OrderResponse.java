package com.tmeras.resellmart.order;

import com.tmeras.resellmart.address.AddressResponse;
import com.tmeras.resellmart.user.UserResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private Integer id;

    private LocalDateTime placedAt;

    private PaymentMethod paymentMethod;

    private AddressResponse billingAddress;

    private AddressResponse deliveryAddress;

    private UserResponse buyer;

    private List<OrderItemResponse> orderItems;
}
