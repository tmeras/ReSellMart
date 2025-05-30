package com.tmeras.resellmart.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEnableRequest {

    @NotNull(message = "Enabled flag must not be empty")
    private Boolean isEnabled;
}
