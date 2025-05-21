package com.tmeras.resellmart.security;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegistrationRequest registrationRequest
    ) throws MessagingException {
        AuthenticationResponse authenticationResponse = authenticationService.register(registrationRequest);
        return new ResponseEntity<>(authenticationResponse,HttpStatus.CREATED);
    }

    @PostMapping("/activation")
    public ResponseEntity<?> activateAccount(
            @RequestParam(name = "code") String code
    ) throws MessagingException {
        authenticationService.activateAccount(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest authenticationRequest
    ) {
        AuthenticationResponse authenticationResponse = authenticationService.login(authenticationRequest);
        String refreshCookie = authenticationResponse.getRefreshTokenCookie();
        authenticationResponse.setRefreshTokenCookie(null);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie)
                .body(authenticationResponse);
    }

    @PostMapping("/refresh")
    public void refreshToken(
            @CookieValue(name = "refresh-token", defaultValue = "") String refreshToken,
            HttpServletResponse response
    ) throws IOException {
        authenticationService.refreshToken(refreshToken, response);
    }

    @PostMapping("/verification")
    public ResponseEntity<AuthenticationResponse> verifyOtp(
            @Valid @RequestBody VerificationRequest verificationRequest
    ) {
        AuthenticationResponse authenticationResponse = authenticationService.verifyOtp(verificationRequest);
        String refreshCookie = authenticationResponse.getRefreshTokenCookie();
        authenticationResponse.setRefreshTokenCookie(null);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie)
                .body(authenticationResponse);
    }
}
