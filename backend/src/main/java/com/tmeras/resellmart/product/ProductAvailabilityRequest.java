package com.tmeras.resellmart.product;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductAvailabilityRequest {

    @NotNull(message = "Deleted flag must not be empty")
    private Boolean isDeleted;
}
