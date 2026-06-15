package com.dnhmd.user_management.controller;

import com.dnhmd.user_management.dto.*;
import com.dnhmd.user_management.security.SecurityUtils;
import com.dnhmd.user_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<UserResponse> getUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive
    ) {
        return userService.getUsers(page, limit, name, isActive);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        return userService.getUser(id, currentUserEmail);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest updateUserRequest
            ) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        return userService.updateUser(id, updateUserRequest, currentUserEmail);
    }

    @PatchMapping("/{id}/password")
    public UserResponse changePassword(
            @PathVariable Long id,
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest
            ) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        return userService.changePassword(id, changePasswordRequest, currentUserEmail);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeRole(
            @PathVariable Long id,
            @RequestBody @Valid ChangeRoleRequest changeRoleRequest
    ) {
        return userService.changeRole(id, changeRoleRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        userService.deleteUser(id, currentUserEmail);
        return ResponseEntity.noContent().build();
    }
}
