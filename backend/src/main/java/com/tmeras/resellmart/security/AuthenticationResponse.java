package com.tmeras.resellmart.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AuthenticationResponse {

    private String accessToken;

    private String refreshTokenCookie;

    private Boolean isMfaEnabled;

    private String qrImageUri;
}
