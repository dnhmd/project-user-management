package com.dnhmd.user_management.controller;

import com.dnhmd.user_management.dto.*;
import com.dnhmd.user_management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public TokenResponse register(RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/login")
    public TokenResponse login(LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        return authService.forgotPassword(forgotPasswordRequest);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        return authService.resetPassword(resetPasswordRequest);
    }

    @PostMapping("refresh")
    public TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return authService.refreshToken(refreshTokenRequest);
    }
}
