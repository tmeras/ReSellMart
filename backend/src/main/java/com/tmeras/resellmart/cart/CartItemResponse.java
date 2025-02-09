package com.tmeras.resellmart.cart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmeras.resellmart.product.ProductResponse;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {

    private Integer id;

    private ProductResponse product;

    private Integer quantity;

    private LocalDateTime addedAt;
}
