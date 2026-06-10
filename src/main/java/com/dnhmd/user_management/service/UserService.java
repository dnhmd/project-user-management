package com.dnhmd.user_management.service;

import com.dnhmd.user_management.dto.CreateUserRequest;
import com.dnhmd.user_management.dto.PagedResponse;
import com.dnhmd.user_management.dto.UserResponse;
import com.dnhmd.user_management.entity.User;

import java.util.Optional;

public interface UserService {

    PagedResponse<UserResponse> getUsers(Integer page, Integer limit, String name, Boolean isActive);
    Optional<UserResponse> getUser(Long id);
    Optional<UserResponse> getUserByEmail(String email);
    UserResponse createUser(CreateUserRequest createUserRequest);
    UserResponse updateUser(Long id, String name, String email);
    UserResponse changePassword(Long id, String oldPassword, String newPassword);
    UserResponse changeRole(Long id, Long roleId);
    UserResponse deleteUser(Long id);
}
