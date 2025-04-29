package com.tmeras.resellmart.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tmeras.resellmart.role.Role;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Integer id;

    private String name;

    private String email;

    private String homeCountry;

    private LocalDate registeredAt;

    private boolean isMfaEnabled;

    private byte[] profileImage;

    private Set<Role> roles;

    private String qrImageUri;
}
