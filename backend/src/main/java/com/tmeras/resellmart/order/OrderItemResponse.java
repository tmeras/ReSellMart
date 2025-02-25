package com.tmeras.resellmart.order;

import com.tmeras.resellmart.product.ProductResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {

    public Integer id;

    public ProductResponse product;

    private Integer productQuantity;
}
