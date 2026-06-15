package com.dnhmd.user_management.service;

import com.dnhmd.user_management.dto.*;

public interface AuthService {

    TokenResponse register(RegisterRequest registerRequest);
    TokenResponse login(LoginRequest loginRequest);
    TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    MessageResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest);
    MessageResponse logout(RefreshTokenRequest refreshTokenRequest);
}
