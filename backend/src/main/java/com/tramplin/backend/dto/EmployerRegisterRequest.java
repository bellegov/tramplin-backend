package com.tramplin.backend.dto;

public record EmployerRegisterRequest(
        String email,
        String password,
        String displayName, // ФИО представителя
        String companyName, // Название компании
        String inn          // ИНН для верификации
) {}