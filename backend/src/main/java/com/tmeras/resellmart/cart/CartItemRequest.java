package com.tmeras.resellmart.cart;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemRequest {

    private Integer id;

    private Integer productId;

    private Integer quantity;

    private Integer userId;
}
