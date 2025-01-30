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

    @NotBlank(message = "Product name must not be empty")
    private String name;

    @NotBlank(message = "Product description must not be empty")
    private String description;

    @NotNull(message = "Price must not be empty")
    @PositiveOrZero(message = "Price must be a non-negative value")
    private Double price;

    @NotNull(message = "Discounted price must not be empty")
    @PositiveOrZero(message = "Discounted price must be a non-negative value")
    private Double discountedPrice;

    @NotNull(message = "Product condition must not be empty ")
    private ProductCondition productCondition;

    @NotNull(message = "Quantity must not be empty")
    @PositiveOrZero(message = "Quantity must be a non-negative value")
    private Integer availableQuantity;

    @NotNull(message = "Product availability must be specified")
    private boolean available;

    @NotNull(message = "Category ID must not be empty")
    private Integer categoryId;
}
