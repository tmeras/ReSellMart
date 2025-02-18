package com.tmeras.resellmart.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Primary flag must be specified")
    private boolean primary;

    @NotBlank(message = "Address type must be specified")
    private AddressType addressType;
}
