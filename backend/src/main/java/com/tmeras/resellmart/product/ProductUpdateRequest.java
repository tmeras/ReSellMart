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
public class ProductUpdateRequest {

    private String name;

    private String description;

    @PositiveOrZero(message = "Price must be a non-negative value")
    @Max(value = MAX_PRODUCT_PRICE, message = "Price must not exceed 10000")
    private Double price;

    private ProductCondition productCondition;

    @PositiveOrZero(message = "Quantity must be a non-negative value")
    @Max(value = MAX_PRODUCT_QUANTITY, message = "Quantity must not exceed 1000")
    private Integer availableQuantity;

    private Integer categoryId;

    private Boolean isDeleted;
}
