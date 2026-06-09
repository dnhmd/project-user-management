package com.dnhmd.user_management.mapper;

import com.dnhmd.user_management.dto.RoleResponse;
import com.dnhmd.user_management.dto.UserResponse;
import com.dnhmd.user_management.entity.Role;
import com.dnhmd.user_management.entity.User;

public class UserMapper {

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getIsActive(),
                toRoleResponse(user.getRole())
        );
    }

    public static RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName()
        );
    }
}
