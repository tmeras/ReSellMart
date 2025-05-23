package com.tmeras.resellmart.order;

import com.tmeras.resellmart.product.ProductCondition;
import com.tmeras.resellmart.user.UserResponse;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {

    private Integer id;

    private OrderItemStatus status;

    private Integer productId;

    private Integer productQuantity;

    private String productName;

    private BigDecimal productPrice;

    private ProductCondition productCondition;

    private byte[] productImage;

    private UserResponse productSeller;
}
