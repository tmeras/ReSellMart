package com.tmeras.resellmart.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {

    @NotNull(message = "Payment method must not be empty")
    @Pattern(
            regexp = "CARD|CASH",
            message = "Invalid payment method"
    )
    private String paymentMethod;

    @NotNull(message = "Billing address ID must not be empty")
    private Integer billingAddressId;

    @NotNull(message = "Delivery address ID must not be empty")
    private Integer deliveryAddressId;

    @NotNull(message = "Buyer ID must not be empty")
    private Integer buyerId;
}
