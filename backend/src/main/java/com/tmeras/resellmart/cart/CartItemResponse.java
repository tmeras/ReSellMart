package com.tmeras.resellmart.cart;

import com.tmeras.resellmart.product.ProductResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {

    private Integer id;

    private ProductResponse product;

    private Integer quantity;

    private LocalDateTime createdAt;
}
