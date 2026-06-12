package com.dnhmd.user_management.service.implementation;

import com.dnhmd.user_management.dto.*;
import com.dnhmd.user_management.entity.Role;
import com.dnhmd.user_management.entity.User;
import com.dnhmd.user_management.exception.BadRequestException;
import com.dnhmd.user_management.exception.ConflictException;
import com.dnhmd.user_management.exception.ResourceNotFoundException;
import com.dnhmd.user_management.mapper.UserMapper;
import com.dnhmd.user_management.repository.RoleRepository;
import com.dnhmd.user_management.repository.UserRepository;
import com.dnhmd.user_management.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PagedResponse<UserResponse> getUsers(Integer page, Integer limit, String name, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, limit);
        Specification<User> userSpecification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null) predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            if (isActive != null) predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> userPage = userRepository.findAll(userSpecification, pageable);

        return new PagedResponse<>(
                userPage.getContent().stream().map(UserMapper::toUserResponse).toList(),
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.isFirst(),
                userPage.isLast()
        );
    }

    @Override
    @Transactional
    public UserResponse getUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) throw new ResourceNotFoundException("User", id.toString());

        return UserMapper.toUserResponse(user.get());
    }

    @Override
    @Transactional
    public UserResponse getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) throw new ResourceNotFoundException("User", email);

        return UserMapper.toUserResponse(user.get());
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        if ((userRepository.findByEmail(createUserRequest.getEmail())).isPresent())
            throw new ConflictException("Email already in use");
        Role defaultRole = roleRepository.findRoleByName(DEFAULT_ROLE).orElseThrow(
                () -> new RuntimeException("Default role not found")
        );
        String hashedPassword = passwordEncoder.encode(createUserRequest.getPassword());

        User savedUser = userRepository.saveAndFlush(
                User.builder()
                        .name(createUserRequest.getName())
                        .email(createUserRequest.getEmail())
                        .hashedPassword(hashedPassword)
                        .isActive(true)
                        .role(defaultRole)
                        .build()
        );

        return UserMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) throw new ResourceNotFoundException("User", id.toString());
        if (updateUserRequest.getName() != null) user.get().setName(updateUserRequest.getName());
        if (updateUserRequest.getEmail() != null) user.get().setEmail(updateUserRequest.getEmail());
        if (updateUserRequest.getName() == null && updateUserRequest.getEmail() == null)
            throw new BadRequestException("No fields to update");
        User modifiedUser = userRepository.saveAndFlush(user.get());

        return UserMapper.toUserResponse(modifiedUser);
    }

    @Override
    @Transactional
    public UserResponse changePassword(Long id, ChangePasswordRequest changePasswordRequest) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) throw new ResourceNotFoundException("User", id.toString());
        if (!isPasswordVerified(changePasswordRequest.getOldPassword(), user.get().getHashedPassword()))
            throw new BadRequestException("Invalid credentials");

        String hashedNewPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        user.get().setHashedPassword(hashedNewPassword);
        User modifiedUser = userRepository.saveAndFlush(user.get());

        return UserMapper.toUserResponse(modifiedUser);
    }

    @Override
    @Transactional
    public UserResponse changeRole(Long id, ChangeRoleRequest changeRoleRequest) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) throw new ResourceNotFoundException("User", id.toString());
        Optional<Role> role = roleRepository.findById(changeRoleRequest.getRoleId());
        if (role.isEmpty()) throw new ResourceNotFoundException("Role", id.toString());
        user.get().setRole(role.get());
        User modifiedUser = userRepository.saveAndFlush(user.get());

        return UserMapper.toUserResponse(modifiedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) throw new ResourceNotFoundException("User", id.toString());
        user.get().setIsActive(false);
        User deletedUser = userRepository.saveAndFlush(user.get());

        UserMapper.toUserResponse(deletedUser);
    }

    private Boolean isPasswordVerified(String providedPassword, String currentPassword) {
        return passwordEncoder.matches(providedPassword, currentPassword);
    }
}
