package com.dnhmd.user_management.service.implementation;

import com.dnhmd.user_management.config.SecurityProperties;
import com.dnhmd.user_management.dto.*;
import com.dnhmd.user_management.entity.RefreshToken;
import com.dnhmd.user_management.entity.User;
import com.dnhmd.user_management.exception.BadRequestException;
import com.dnhmd.user_management.exception.ResourceNotFoundException;
import com.dnhmd.user_management.repository.RefreshTokenRepository;
import com.dnhmd.user_management.repository.UserRepository;
import com.dnhmd.user_management.security.JwtService;
import com.dnhmd.user_management.service.AuthService;
import com.dnhmd.user_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityProperties securityProperties;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public TokenResponse register(RegisterRequest registerRequest) {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                registerRequest.getName(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        UserResponse userResponse = userService.createUser(createUserRequest);
        User user = userRepository.findById(userResponse.getId())
                .orElseThrow(() -> new RuntimeException("Internal server error"));
        String accessToken = jwtService.generateAccessToken(userResponse.getEmail());
        RefreshToken refreshToken = refreshTokenRepository.saveAndFlush(
                RefreshToken.builder()
                        .token(jwtService.generateRefreshToken())
                        .user(user)
                        .expiresAt(LocalDateTime.now()
                                .plusDays(securityProperties.getRefreshTokenExpireDays()))
                        .isRevoked(false)
                        .build()
        );

        return new TokenResponse(accessToken, refreshToken.getToken(), "bearer");
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        String accessToken = jwtService.generateAccessToken(loginRequest.getEmail());
        RefreshToken refreshToken = refreshTokenRepository.saveAndFlush(
                RefreshToken.builder()
                        .token(jwtService.generateRefreshToken())
                        .user(user)
                        .expiresAt(LocalDateTime.now()
                                .plusDays(securityProperties.getRefreshTokenExpireDays()))
                        .isRevoked(false)
                        .build()
        );

        return new TokenResponse(accessToken, refreshToken.getToken(), "bearer");
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        RefreshToken currentRefreshToken = refreshTokenRepository.findByToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token", refreshTokenRequest.getRefreshToken()));
        User user = currentRefreshToken.getUser();
        if (currentRefreshToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Refresh token expired. Please login again.");
        revokeRefreshToken(currentRefreshToken);
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenRepository.saveAndFlush(
                RefreshToken.builder()
                        .token(jwtService.generateRefreshToken())
                        .user(user)
                        .expiresAt(LocalDateTime.now()
                                .plusDays(securityProperties.getRefreshTokenExpireDays()))
                        .isRevoked(false)
                        .build()
        );

        return new TokenResponse(accessToken, refreshToken.getToken(), "bearer");
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        Optional<User> user = userRepository.findByEmail(forgotPasswordRequest.getEmail());
        if (user.isPresent()) {
            String passwordResetToken = jwtService.generatePasswordResetToken();
            user.get().setPasswordResetToken(passwordResetToken);
            user.get().setPasswordResetTokenExpiresAt(LocalDateTime.now()
                    .plusMinutes(securityProperties.getPasswordResetTokenExpireMinutes()));
            System.out.println(passwordResetToken);
            userRepository.saveAndFlush(user.get());
        }
        return new MessageResponse("If an account with that email exists, a reset link has been sent.");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String passwordResetToken = resetPasswordRequest.getToken();
        User user = userRepository.findByPasswordResetToken(passwordResetToken)
                .orElseThrow(() -> new BadRequestException("Password reset token unavailable"));
        if (user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Password reset token expired");
        String hashedPassword = passwordEncoder.encode(resetPasswordRequest.getNewPassword());
        user.setHashedPassword(hashedPassword);
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.saveAndFlush(user);

        return new MessageResponse("Password reset is successful");
    }

    @Override
    @Transactional
    public MessageResponse logout(RefreshTokenRequest refreshTokenRequest) {
        RefreshToken currentRefreshToken = refreshTokenRepository.findByToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token", refreshTokenRequest.getRefreshToken()));
        revokeRefreshToken(currentRefreshToken);

        return new MessageResponse("Logout is successful");
    }

    private void revokeRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.getIsRevoked())
            throw new BadRequestException("Refresh token revoked already. Please login again.");
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.saveAndFlush(refreshToken);
    }
}
