package com.tmeras.resellmart.order;

import com.tmeras.resellmart.product.ProductResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {

    private Integer id;

    private ProductResponse product;

    private Integer productQuantity;
}
