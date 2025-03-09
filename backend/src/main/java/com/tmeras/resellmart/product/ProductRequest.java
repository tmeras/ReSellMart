package com.tmeras.resellmart.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {

    private Integer id;

    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotBlank(message = "Description must not be empty")
    private String description;

    @NotNull(message = "Price must not be empty")
    @PositiveOrZero(message = "Price must be a non-negative value")
    private Double price;

    @NotNull(message = "Product condition must not be empty ")
    private ProductCondition productCondition;

    @NotNull(message = "Quantity must not be empty")
    @PositiveOrZero(message = "Quantity must be a non-negative value")
    private Integer availableQuantity;

    private Boolean isDeleted;

    @NotNull(message = "Category ID must not be empty")
    private Integer categoryId;
}
