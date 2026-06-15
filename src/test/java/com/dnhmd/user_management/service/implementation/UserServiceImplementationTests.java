package com.dnhmd.user_management.service.implementation;

import com.dnhmd.user_management.dto.*;
import com.dnhmd.user_management.entity.Role;
import com.dnhmd.user_management.entity.User;
import com.dnhmd.user_management.exception.BadRequestException;
import com.dnhmd.user_management.exception.ConflictException;
import com.dnhmd.user_management.exception.ResourceNotFoundException;
import com.dnhmd.user_management.exception.UnauthorizedException;
import com.dnhmd.user_management.repository.RoleRepository;
import com.dnhmd.user_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplementationTests {

    private Role mockRole;
    private User mockUser;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImplementation userServiceImplementation;

    @BeforeEach
    void setUp() {
        mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setName("USER");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setName("Test User");
        mockUser.setIsActive(true);
        mockUser.setRole(mockRole);
    }

    @Test
    void getUser_shouldReturnUser_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserResponse response = userServiceImplementation.getUser(1L, "test@test.com");

        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    void getUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userServiceImplementation.getUser(1L, "test@test.com")
        );
    }

    @Test
    void getUser_shouldThrowException_whenUserIsNotOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        Authentication authentication = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .when(authentication).getAuthorities();
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(UnauthorizedException.class, () ->
                userServiceImplementation.getUser(1L, "test@test.in")
        );
    }

    @Test
    void createUser_shouldReturnUser_whenEmailIsNotTaken() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findRoleByName("USER")).thenReturn(Optional.of(mockRole));
        when(passwordEncoder.encode("User@User123")).thenReturn("hashedPassword");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(mockUser);

        CreateUserRequest request = new CreateUserRequest("Test User", "test@test.com", "User@User123");

        UserResponse response = userServiceImplementation.createUser(request);

        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        CreateUserRequest request = new CreateUserRequest("Test User", "test@test.com", "User@User123");

        assertThrows(ConflictException.class, () -> userServiceImplementation.createUser(request));
    }

    @Test
    void createUser_shouldThrowException_whenDefaultRoleIsNotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findRoleByName("USER")).thenReturn(Optional.empty());

        CreateUserRequest request = new CreateUserRequest("Test User", "test@test.com", "User@User123");

        assertThrows(RuntimeException.class, () -> userServiceImplementation.createUser(request));
    }

    @Test
    void updateUser_shouldReturnUser_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(mockUser);

        UpdateUserRequest request = new UpdateUserRequest("Test User", "test@test.com");

        UserResponse response = userServiceImplementation.updateUser(1L, request, "test@test.com");

        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateUserRequest request = new UpdateUserRequest("Test User", "test@test.com");

        assertThrows(ResourceNotFoundException.class, () ->
                userServiceImplementation.updateUser(1L, request, "test@test.com")
        );
    }

    @Test
    void updateUser_shouldThrowException_whenUserIsNotOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UpdateUserRequest request = new UpdateUserRequest("Test User", "test@test.com");

        assertThrows(UnauthorizedException.class, () ->
                userServiceImplementation.updateUser(1L, request, "test@test.in")
        );
    }

    @Test
    void updateUser_shouldThrowException_whenNoFieldsToUpdate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UpdateUserRequest request = new UpdateUserRequest();

        assertThrows(BadRequestException.class, () ->
                userServiceImplementation.updateUser(1L, request, "test@test.com")
        );
    }

    @Test
    void changePassword_shouldReturnUser_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(mockUser);

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

        UserResponse response = userServiceImplementation.changePassword(1L, request, "test@test.com");

        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    void changePassword_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

        assertThrows(ResourceNotFoundException.class, () ->
                userServiceImplementation.changePassword(1L, request, "test@test.com")
        );
    }

    @Test
    void changePassword_shouldThrowException_whenUserIsNotOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

        assertThrows(UnauthorizedException.class, () ->
                userServiceImplementation.changePassword(1L, request, "test@test.in")
        );
    }

    @Test
    void changePassword_shouldThrowException_whenPasswordIsNotVerified() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

        assertThrows(BadRequestException.class, () ->
                userServiceImplementation.changePassword(1L, request, "test@test.com")
        );
    }

    @Test
    void changeRole_shouldReturnUser_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(mockUser);

        ChangeRoleRequest request = new ChangeRoleRequest(1L);

        UserResponse response = userServiceImplementation.changeRole(1L, request);

        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    void changeRole_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ChangeRoleRequest request = new ChangeRoleRequest(1L);

        assertThrows(ResourceNotFoundException.class, () ->
                userServiceImplementation.changeRole(1L, request)
        );
    }

    @Test
    void changeRole_shouldThrowException_whenRoleNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        ChangeRoleRequest request = new ChangeRoleRequest(1L);

        assertThrows(ResourceNotFoundException.class, () ->
                userServiceImplementation.changeRole(1L, request)
        );
    }

    @Test
    void deleteUser_shouldReturnIsActiveFalse_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(null);

        userServiceImplementation.deleteUser(1L, "test@test.com");

        assertFalse(mockUser.getIsActive());
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userServiceImplementation.deleteUser(1L, "test@test.com")
        );
    }

    @Test
    void deleteUser_shouldThrowException_whenUserIsNotOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        assertThrows(UnauthorizedException.class, () ->
                userServiceImplementation.deleteUser(1L, "test@test.in")
        );
    }
}
