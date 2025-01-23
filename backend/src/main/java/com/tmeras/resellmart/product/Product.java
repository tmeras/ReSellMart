package com.tmeras.resellmart.product;

import com.tmeras.resellmart.category.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    private String description;

    private Double price;

    private Double discountedPrice;

    @Enumerated(EnumType.STRING)
    private ProductCondition productCondition;

    private Integer availableQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    // TODO: Add relationship to seller

}
