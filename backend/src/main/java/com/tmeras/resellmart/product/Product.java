package com.tmeras.resellmart.product;

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

    private Double discounted_price;

    @Enumerated(EnumType.STRING)
    private String condition;

    private Integer available_quantity;

    // TODO: Add relationship to category

    // TODO: Add relationship to seller

}
