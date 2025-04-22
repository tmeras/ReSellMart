package com.tmeras.resellmart.product;

import com.tmeras.resellmart.common.AppConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import static com.tmeras.resellmart.common.AppConstants.MAX_PRODUCT_PRICE;
import static com.tmeras.resellmart.common.AppConstants.MAX_PRODUCT_QUANTITY;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {

    private Integer id;

    @NotBlank(message = "Name must not be empty")
    // TODO: Limit length?
    private String name;

    @NotBlank(message = "Description must not be empty")
    private String description;

    @NotNull(message = "Price must not be empty")
    @PositiveOrZero(message = "Price must be a non-negative value")
    @Max(value = MAX_PRODUCT_PRICE, message = "Price must not exceed 10000")
    private Double price;

    @NotNull(message = "Product condition must not be empty ")
    private ProductCondition productCondition;

    @NotNull(message = "Quantity must not be empty")
    @PositiveOrZero(message = "Quantity must be a non-negative value")
    @Max(value = MAX_PRODUCT_QUANTITY, message = "Quantity must not exceed 1000")
    private Integer availableQuantity;

    @NotNull(message = "Category ID must not be empty")
    private Integer categoryId;
}
