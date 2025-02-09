package com.tmeras.resellmart.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemRequest {

    @NotNull(message = "Product ID must not be empty")
    private Integer productId;

    @Positive(message = "Product quantity must be a positive value")
    private Integer quantity;

    private Integer userId;
}
