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
import com.dnhmd.user_management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplementationTests {

    private User mockUser;
    private User mockUserExpiredPasswordToken;
    private UserResponse mockUserResponse;
    private RefreshToken mockRefreshToken;
    private RefreshToken mockExpiredRefreshToken;
    private RefreshToken mockRevokedRefreshToken;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private SecurityProperties securityProperties;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImplementation authServiceImplementation;

    @BeforeEach
    void setUp() {
        RoleResponse mockRoleResponse = new RoleResponse();
        mockRoleResponse.setId(1L);
        mockRoleResponse.setName("USER");

        mockUser = new User();
        mockUser.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(10));

        mockUserExpiredPasswordToken = new User();
        mockUserExpiredPasswordToken.setPasswordResetTokenExpiresAt(LocalDateTime.now().minusMinutes(10));

        mockUserResponse = new UserResponse();
        mockUserResponse.setId(1L);
        mockUserResponse.setName("Test User");
        mockUserResponse.setEmail("test@test.com");
        mockUserResponse.setIsActive(true);
        mockUserResponse.setRole(mockRoleResponse);

        mockRefreshToken = new RefreshToken();
        mockRefreshToken.setId(1L);
        mockRefreshToken.setToken("token");
        mockRefreshToken.setUser(new User());
        mockRefreshToken.setIsRevoked(false);
        mockRefreshToken.setCreatedAt(LocalDateTime.now());
        mockRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        mockExpiredRefreshToken = new RefreshToken();
        mockExpiredRefreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        mockExpiredRefreshToken.setIsRevoked(false);

        mockRevokedRefreshToken = new RefreshToken();
        mockRevokedRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        mockRevokedRefreshToken.setIsRevoked(true);
    }

    @Test
    void register_shouldReturnTokenResponse_whenRegistered() {
        when(userService.createUser(any())).thenReturn(mockUserResponse);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
        when(refreshTokenRepository.saveAndFlush(any())).thenReturn(mockRefreshToken);
        when(securityProperties.getRefreshTokenExpireDays()).thenReturn(7);

        RegisterRequest request = new RegisterRequest("Test User", "test@test.com", "password123@123");
        TokenResponse response = authServiceImplementation.register(request);

        assertEquals("bearer", response.getTokenType());
    }

    @Test
    void register_shouldThrowException_whenUserNotFound() {
        when(userService.createUser(any())).thenReturn(mockUserResponse);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RegisterRequest request = new RegisterRequest("Test User", "test@test.com", "password123@123");

        assertThrows(RuntimeException.class, () -> authServiceImplementation.register(request));
    }

    @Test
    void login_shouldReturnTokenResponse_whenLoggedIn() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("test@test.com", null));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));
        when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
        when(refreshTokenRepository.saveAndFlush(any())).thenReturn(mockRefreshToken);
        when(securityProperties.getRefreshTokenExpireDays()).thenReturn(7);

        LoginRequest request = new LoginRequest("test@test.com", "password123@123");
        TokenResponse response = authServiceImplementation.login(request);

        assertEquals("bearer", response.getTokenType());
    }

    @Test
    void login_shouldThrowException_whenWrongCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        LoginRequest request = new LoginRequest("test@test.com", "password123@123");

        assertThrows(BadCredentialsException.class, () -> authServiceImplementation.login(request));
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("test@test.com", null));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("test@test.com", "password123@123");

        assertThrows(BadCredentialsException.class, () -> authServiceImplementation.login(request));
    }

    @Test
    void refreshToken_shouldReturnTokenResponse_whenUserIsLoggedIn() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(mockRefreshToken));
        when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
        when(refreshTokenRepository.saveAndFlush(any())).thenReturn(mockRefreshToken);
        when(securityProperties.getRefreshTokenExpireDays()).thenReturn(7);

        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken");
        TokenResponse response = authServiceImplementation.refreshToken(request);

        assertEquals("bearer", response.getTokenType());
    }

    @Test
    void refreshToken_shouldThrowException_whenTokenNotFound() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken");

        assertThrows(ResourceNotFoundException.class, () -> authServiceImplementation.refreshToken(request));
    }

    @Test
    void refreshToken_shouldThrowException_whenTokenExpired() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(mockExpiredRefreshToken));

        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken");

        assertThrows(BadRequestException.class, () -> authServiceImplementation.refreshToken(request));
    }

    @Test
    void refreshToken_shouldThrowException_whenTokenIsRevoke() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(mockRevokedRefreshToken));

        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken");

        assertThrows(BadRequestException.class, () -> authServiceImplementation.refreshToken(request));
    }

    @Test
    void forgotPassword_shouldReturnMessageResponse_whenUserIsAvailable() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));
        when(jwtService.generatePasswordResetToken()).thenReturn("passwordResetToken");
        when(securityProperties.getPasswordResetTokenExpireMinutes()).thenReturn(10);
        when(userRepository.saveAndFlush(any())).thenReturn(new User());

        ForgotPasswordRequest request = new ForgotPasswordRequest("test@test.com");
        MessageResponse response = authServiceImplementation.forgotPassword(request);

        assertEquals("If an account with that email exists, a reset link has been sent.", response.getMessage());
    }

    @Test
    void forgotPassword_shouldReturnMessageResponse_whenUserIsUnavailable() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        ForgotPasswordRequest request = new ForgotPasswordRequest("test@test.com");
        MessageResponse response = authServiceImplementation.forgotPassword(request);

        assertEquals("If an account with that email exists, a reset link has been sent.", response.getMessage());
    }

    @Test
    void resetPassword_shouldReturnMessageResponse_whenUserIsAvailable() {
        when(userRepository.findByPasswordResetToken(any())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.saveAndFlush(any())).thenReturn(mockUser);

        ResetPasswordRequest request = new ResetPasswordRequest("token", "newPassword");
        MessageResponse response = authServiceImplementation.resetPassword(request);

        assertEquals("Password reset is successful", response.getMessage());
    }

    @Test
    void resetPassword_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByPasswordResetToken(any())).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest("token", "newPassword");

        assertThrows(BadRequestException.class, () -> authServiceImplementation.resetPassword(request));
    }

    @Test
    void resetPassword_shouldThrowException_whenTokenIsExpired() {
        when(userRepository.findByPasswordResetToken(any())).thenReturn(Optional.of(mockUserExpiredPasswordToken));

        ResetPasswordRequest request = new ResetPasswordRequest("token", "newPassword");

        assertThrows(BadRequestException.class, () -> authServiceImplementation.resetPassword(request));
    }

    @Test
    void logout_shouldLogout_whenRefreshTokenIsAvailable() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(mockRefreshToken));
        when(refreshTokenRepository.saveAndFlush(any())).thenReturn(mockRefreshToken);

        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken");
        MessageResponse response = authServiceImplementation.logout(request);

        assertEquals("Logout is successful", response.getMessage());
    }

    @Test
    void logout_shouldThrowException_whenTokenNotFound() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken");

        assertThrows(ResourceNotFoundException.class, () -> authServiceImplementation.logout(request));
    }

    @Test
    void logout_shouldThrowException_whenTokenIsAlreadyRevoked() {
        when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.of(mockRevokedRefreshToken));

        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken");

        assertThrows(BadRequestException.class, () -> authServiceImplementation.logout(request));
    }
}
