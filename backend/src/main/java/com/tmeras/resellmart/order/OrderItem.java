package com.tmeras.resellmart.order;

import com.tmeras.resellmart.product.ProductCondition;
import com.tmeras.resellmart.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    // Below fields make up a snapshot of product details at time of order
    private Integer productId;

    private Integer productQuantity;

    private String productName;

    private BigDecimal productPrice;

    @Enumerated(EnumType.STRING)
    private ProductCondition productCondition;

    // Path to the product image stored on the server
    private String productImagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User productSeller;
}
