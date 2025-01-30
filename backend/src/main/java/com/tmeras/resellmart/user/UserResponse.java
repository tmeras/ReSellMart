package com.tmeras.resellmart.user;

import com.tmeras.resellmart.role.Role;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String name;

    private String email;

    private String homeCountry;

    private boolean mfaEnabled;

    private byte[] profileImage;

    private Set<Role> roles;
}
