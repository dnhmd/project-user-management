package com.dnhmd.user_management.service;

import com.dnhmd.user_management.dto.*;

public interface UserService {

    PagedResponse<UserResponse> getUsers(Integer page, Integer limit, String name, Boolean isActive);
    UserResponse getUser(Long id, String currentUserEmail);
    UserResponse getUserByEmail(String email);
    UserResponse createUser(CreateUserRequest createUserRequest);
    UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest, String currentUserEmail);
    UserResponse changePassword(Long id, ChangePasswordRequest changePasswordRequest, String currentUserEmail);
    UserResponse changeRole(Long id, ChangeRoleRequest changeRoleRequest);
    void deleteUser(Long id, String currentUserEmail);
}
