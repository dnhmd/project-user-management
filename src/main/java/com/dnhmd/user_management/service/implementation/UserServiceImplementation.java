package com.dnhmd.user_management.service.implementation;

import com.dnhmd.user_management.dto.CreateUserRequest;
import com.dnhmd.user_management.dto.PagedResponse;
import com.dnhmd.user_management.dto.UserResponse;
import com.dnhmd.user_management.entity.User;
import com.dnhmd.user_management.repository.UserRepository;
import com.dnhmd.user_management.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;

    @Override
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
                userPage.getContent(),
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.isFirst(),
                userPage.isLast()
        );
    }

    @Override
    public Optional<UserResponse> getUser(Long id) {
        Optional<User> user = userRepository.findById(id);
    }

    @Override
    public Optional<UserResponse> getUserByEmail(String email) {
        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isEmpty()) {
            return
        }
        return UserResponse(
                user.get().getId(),
                user.get().getName(),
                user.get().getEmail(),
                user.get().getIsActive(),
                user.get().
        );
    }

    @Override
    public UserResponse createUser(CreateUserRequest) {
        return null;
    }

    @Override
    public Optional<User> updateUser(Long id, String name, String email) {
        return Optional.empty();
    }

    @Override
    public Optional<User> changePassword(Long id, String oldPassword, String newPassword) {
        return Optional.empty();
    }

    @Override
    public Optional<User> changeRole(Long id, Integer roleId) {
        return Optional.empty();
    }

    @Override
    public Optional<User> deleteUser(Long id) {
        return Optional.empty();
    }
}
