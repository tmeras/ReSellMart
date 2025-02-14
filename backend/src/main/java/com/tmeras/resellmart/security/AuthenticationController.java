package com.tmeras.resellmart.security;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegistrationRequest registrationRequest
    ) throws MessagingException {
        return new ResponseEntity<>(authenticationService.register(registrationRequest),HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest authenticationRequest
    ) {
        return new ResponseEntity<>(authenticationService.login(authenticationRequest), HttpStatus.OK);
    }

    @PostMapping("/activation")
    public ResponseEntity<?> activateAccount(
            @RequestParam(name = "code") String code
    ) throws MessagingException {
        authenticationService.activateAccount(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @PostMapping("/verification")
    public ResponseEntity<AuthenticationResponse> verifyOtp(
            @Valid @RequestBody VerificationRequest verificationRequest
    ) {
        return new ResponseEntity<>(authenticationService.verifyOtp(verificationRequest), HttpStatus.OK);
    }
}
