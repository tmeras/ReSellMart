package com.tmeras.resellmart.product;

import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal previousPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition")
    private ProductCondition condition;

    private Integer availableQuantity;

    private ZonedDateTime listedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User seller;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "productId", nullable = false)
    private List<ProductImage> images = new ArrayList<>();
}
