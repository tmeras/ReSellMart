package com.tmeras.resellmart.security;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email is not in a valid format")
    private String email;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one special character."
    )
    private String password;

    @NotBlank(message = "Home country must not be empty")
    private String homeCountry;

    private boolean mfaEnabled;
}
