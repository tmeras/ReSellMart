package com.tmeras.resellmart.order;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {

    @NotNull(message = "Billing address ID must not be empty")
    private Integer billingAddressId;

    @NotNull(message = "Delivery address ID must not be empty")
    private Integer deliveryAddressId;
}
