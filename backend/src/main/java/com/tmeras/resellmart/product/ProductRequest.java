package com.tmeras.resellmart.product;

import com.tmeras.resellmart.common.AppConstants;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

import static com.tmeras.resellmart.common.AppConstants.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {

    private Integer id;

    @NotBlank(message = "Name must not be empty")
    @Size(max = MAX_PRODUCT_NAME_LENGTH, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Description must not be empty")
    @Size(max= MAX_PRODUCT_DESCRIPTION_LENGTH, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Price must not be empty")
    @PositiveOrZero(message = "Price must be a non-negative value")
    @Max(value = MAX_PRODUCT_PRICE, message = "Price must not exceed 10000")
    @Digits(integer = 5, fraction = 2, message = "Price must be a decimal number with up to 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Product condition must not be empty ")
    private ProductCondition productCondition;

    @NotNull(message = "Quantity must not be empty")
    @PositiveOrZero(message = "Quantity must be a non-negative value")
    @Max(value = MAX_PRODUCT_QUANTITY, message = "Quantity must not exceed 1000")
    private Integer availableQuantity;

    @NotNull(message = "Category ID must not be empty")
    private Integer categoryId;
}
