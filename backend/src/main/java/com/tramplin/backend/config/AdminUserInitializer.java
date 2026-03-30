package com.tramplin.backend.config;

import com.tramplin.backend.model.User;
import com.tramplin.backend.model.Role;
import com.tramplin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${initial-admin.email}")
    private String adminEmail;

    @Value("${initial-admin.password}")
    private String adminPassword;

    @Value("${initial-admin.display-name}")
    private String adminDisplayName;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByRole(Role.ROLE_ADMIN)) {

            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .displayName(adminDisplayName)
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("======================================================");
            System.out.println("Первый администратор создан с email: " + adminEmail);
            System.out.println("======================================================");
        }
    }
}