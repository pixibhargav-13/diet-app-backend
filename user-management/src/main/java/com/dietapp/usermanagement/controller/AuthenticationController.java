package com.dietapp.usermanagement.controller;

import com.dietapp.api.usermanagement.api.AuthenticationApi;
import com.dietapp.api.usermanagement.model.*;
import com.dietapp.usermanagement.service.AuthenticationService;
import com.dietapp.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest registerRequest) {
        AuthResponse response = authenticationService.register(
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        return ResponseEntity.ok(
                authenticationService.login(loginRequest.getEmail(), loginRequest.getPassword())
        );
    }

    @Override
    public ResponseEntity<MessageResponse> forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        return ResponseEntity.ok(userService.forgotPassword(forgotPasswordRequest.getEmail()));
    }

    @Override
    public ResponseEntity<MessageResponse> resetPassword(ResetPasswordRequest resetPasswordRequest) {
        return ResponseEntity.ok(
                userService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword())
        );
    }
}
