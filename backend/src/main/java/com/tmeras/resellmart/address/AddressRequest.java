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

    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotBlank(message = "Country must not be empty")
    private String country;

    @NotBlank(message = "Street must not be empty")
    private String street;

    @NotBlank(message = "State must not be empty")
    private String state;

    @NotBlank(message = "City must not be empty")
    private String city;

    @NotBlank(message = "Postal code must not be empty")
    private String postalCode;

    @Pattern(
            regexp = "^\\+[0-9 ()-]{7,25}$",
            message = "Phone number must start with '+' and contain" +
                    " only digits, spaces, parentheses, or dashes"
    )
    private String phoneNumber;

    private Boolean isMain;

    @NotNull(message = "Address type must not be empty")
    @Pattern(
            regexp = "HOME|WORK|BILLING|SHIPPING",
            message = "Invalid address type"
    )
    private String addressType;
}
