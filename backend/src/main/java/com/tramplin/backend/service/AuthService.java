package com.tramplin.backend.service;

import com.tramplin.backend.dto.*;
import com.tramplin.backend.model.EmployerProfile;
import com.tramplin.backend.model.SeekerProfile;
import com.tramplin.backend.model.User;
import com.tramplin.backend.model.Role;
import com.tramplin.backend.model.VerificationStatus;
import com.tramplin.backend.repository.EmployerProfileRepository;
import com.tramplin.backend.repository.SeekerProfileRepository;
import com.tramplin.backend.repository.UserRepository;
import com.tramplin.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final EmployerProfileRepository employerRepo;
    private final SeekerProfileRepository seekerRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse registerEmployer(EmployerRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email уже занят");
        }

        // 1. Создаем юзера
        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(Role.ROLE_EMPLOYER)
                .build();
        userRepository.save(user);

        // 2. Сразу создаем профиль компании с данными из запроса
        var profile = EmployerProfile.builder()
                .user(user)
                .companyName(request.companyName())
                .inn(request.inn())
                .verificationStatus(VerificationStatus.PENDING) // Статус: Ожидание
                .build();
        employerRepo.save(profile);

        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse registerSeeker(SeekerRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email уже занят");
        }

        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(Role.ROLE_SEEKER)
                .build();
        userRepository.save(user);

        var profile = SeekerProfile.builder()
                .user(user)
                .university(request.university())
                .isPublic(true)
                .build();
        seekerRepo.save(profile);

        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var user = userRepository.findByEmail(request.email()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }


    public void createCurator(CreateCuratorRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email " + request.email() + " уже занят");
        }

        // 1. Создаем пользователя
        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(Role.ROLE_ADMIN) // Роль жестко прописана как ADMIN!
                .build();
        userRepository.save(user);

        // 2. Важно: мы НЕ создаем для админа профиль соискателя или работодателя.
        // Он просто пользователь с особыми правами.
    }

}