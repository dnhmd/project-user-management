package com.dnhmd.user_management.service;

import com.dnhmd.user_management.dto.CreateUserRequest;
import com.dnhmd.user_management.dto.PagedResponse;
import com.dnhmd.user_management.dto.UserResponse;

import java.util.Optional;

public interface UserService {

    PagedResponse<UserResponse> getUsers(Integer page, Integer limit, String name, Boolean isActive);
    Optional<UserResponse> getUser(Long id);
    Optional<User> getUserByEmail(String email);
    UserResponse createUser(CreateUserRequest);
    Optional<User> updateUser(Long id, String name, String email);
    Optional<User> changePassword(Long id, String oldPassword, String newPassword);
    Optional<User> changeRole(Long id, Integer roleId);
    Optional<User> deleteUser(Long id);
}
