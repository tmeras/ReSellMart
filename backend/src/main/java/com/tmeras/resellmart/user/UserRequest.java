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

    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotNull(message = "Home country must not be empty")
    private String homeCountry;

    @NotNull(message = "MFA preference must be specified")
    private Boolean isMfaEnabled;
}
