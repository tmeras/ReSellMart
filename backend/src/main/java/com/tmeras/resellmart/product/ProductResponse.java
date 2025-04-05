package com.tmeras.resellmart.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.user.UserResponse;
import lombok.*;

import java.util.List;

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

    private Double previousPrice;

    private ProductCondition productCondition;

    private Integer availableQuantity;

    private Boolean isDeleted; // TODO: Investigate changing to primitive

    private List<ProductImageResponse> images;

    private CategoryResponse category;

    private UserResponse seller;
}
