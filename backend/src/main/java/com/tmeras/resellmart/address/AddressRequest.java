package com.tmeras.resellmart.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressRequest {

    @NotBlank(message = "Country must not be null")
    private String country;

    @NotBlank(message = "Street must not be null")
    private String street;

    @NotBlank(message = "State must not be null")
    private String state;

    @NotBlank(message = "City must not be null")
    private String city;

    @NotBlank(message = "Postal code must not be null")
    private String postalCode;

    private boolean main;

    private boolean deleted;

    @NotNull(message = "Address type must not be null")
    @Pattern(
            regexp = "HOME|WORK|BILLING|SHIPPING",
            message = "Invalid address type"
    )
    private String addressType;
}
