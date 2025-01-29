package com.tmeras.resellmart.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmeras.resellmart.category.CategoryResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private Integer id;

    private String name;

    private String description;

    private Double price;

    private Double discountedPrice;

    private ProductCondition productCondition;

    private Integer availableQuantity;

    private boolean available;

    private CategoryResponse categoryResponse;

    // TODO: Return user response
    private String sellerName;
}
