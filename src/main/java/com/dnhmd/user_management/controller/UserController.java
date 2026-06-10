package com.dnhmd.user_management.controller;

import com.dnhmd.user_management.dto.*;
import com.dnhmd.user_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping
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
        return userService.getUser(id);
    }

    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest updateUserRequest
            ) {
        return userService.updateUser(id, updateUserRequest);
    }

    @PatchMapping("/{id}/password")
    public UserResponse changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest changePasswordRequest
            ) {
        return userService.changePassword(id, changePasswordRequest);
    }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(
            @PathVariable Long id,
            @RequestBody ChangeRoleRequest changeRoleRequest
    ) {
        return userService.changeRole(id, changeRoleRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
