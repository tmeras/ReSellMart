package com.tmeras.resellmart.order;

import com.tmeras.resellmart.product.Product;
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

    // Reference to product with up-to-date details
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    // Below fields make up a snapshot of product details at time of order
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
