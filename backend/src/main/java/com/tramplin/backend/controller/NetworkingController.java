package com.tramplin.backend.controller;

import com.tramplin.backend.dto.RecommendationRequest;
import com.tramplin.backend.dto.SeekerProfileResponse;
import com.tramplin.backend.service.NetworkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/networking")
@RequiredArgsConstructor
public class NetworkingController {

    private final NetworkingService networkingService;

    // 1. Поиск соискателей (выдает только тех, у кого isPublic = true)
    @GetMapping("/search")
    public ResponseEntity<List<SeekerProfileResponse>> searchPeople(@RequestParam String name) {
        return ResponseEntity.ok(networkingService.searchPeople(name));
    }

    // 2. Добавить в контакты (друзья)
    @PostMapping("/friends/{friendId}")
    public ResponseEntity<String> addFriend(@PathVariable Long friendId) {
        networkingService.addFriend(friendId);
        return ResponseEntity.ok("Запрос в друзья отправлен/принят");
    }

    // 3. Список моих контактов (друзей)
    @GetMapping("/friends")
    public ResponseEntity<List<SeekerProfileResponse>> getMyFriends() {
        return ResponseEntity.ok(networkingService.getMyFriends());
    }

    // 4. Рекомендовать друга на вакансию (Та самая фича из ТЗ!)
    @PostMapping("/recommend")
    public ResponseEntity<String> recommend(@RequestBody RecommendationRequest request) {
        networkingService.recommendFriend(request.friendId(), request.opportunityId(), request.message());
        return ResponseEntity.ok("Рекомендация успешно отправлена");
    }
}