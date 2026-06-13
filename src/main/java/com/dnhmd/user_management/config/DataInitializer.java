package com.dnhmd.user_management.config;

import com.dnhmd.user_management.entity.Role;
import com.dnhmd.user_management.entity.User;
import com.dnhmd.user_management.repository.RoleRepository;
import com.dnhmd.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AppProperties appProperties;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        Optional<User> user = userRepository.findByEmail(appProperties.getAdminEmail());
        if (user.isPresent()) return;
        Role adminRole = roleRepository.findRoleByName("ADMIN").orElseThrow(
                () -> new RuntimeException("Role Admin not found"));
        String hashedPassword = passwordEncoder.encode(appProperties.getAdminPassword());

        userRepository.saveAndFlush(
                User.builder()
                        .name("Admin")
                        .email(appProperties.getAdminEmail())
                        .hashedPassword(hashedPassword)
                        .isActive(true)
                        .role(adminRole)
                        .build()
        );
    }
}
