package com.tmeras.resellmart.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Name should not be empty")
    private String name;

    @Email(message = "Email is not in a valid format")
    @NotBlank
    private String email;

    @NotNull(message = "Home country should not be empty")
    private String homeCountry;

    @NotNull(message = "MFA preference must be specified")
    private boolean mfaEnabled;
}
