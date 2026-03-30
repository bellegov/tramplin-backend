package com.tramplin.backend.controller;

import com.tramplin.backend.dto.OpportunityResponse;
import com.tramplin.backend.repository.FavoriteRepository;
import com.tramplin.backend.model.Favorite;
import com.tramplin.backend.model.User;
import com.tramplin.backend.repository.UserRepository;
import com.tramplin.backend.model.Opportunity;
import com.tramplin.backend.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final OpportunityService opportunityService;

    // ---  РУЧКА ДЛЯ СПИСКА ИЗБРАННОГО ---
    @GetMapping
    public ResponseEntity<List<OpportunityResponse>> getMyFavorites() {
        return ResponseEntity.ok(opportunityService.getMyFavorites());
    }

    @PostMapping("/{opportunityId}")
    public ResponseEntity<String> add(@PathVariable Long opportunityId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        if (favoriteRepository.findByUserIdAndOpportunityId(user.getId(), opportunityId).isEmpty()) {
            favoriteRepository.save(Favorite.builder()
                    .user(user)
                    .opportunity(Opportunity.builder().id(opportunityId).build())
                    .build());
        }
        return ResponseEntity.ok("Добавлено в избранное");
    }

    @DeleteMapping("/{opportunityId}")
    @Transactional
    public ResponseEntity<String> remove(@PathVariable Long opportunityId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        favoriteRepository.deleteByUserIdAndOpportunityId(user.getId(), opportunityId);
        return ResponseEntity.ok("Удалено из избранного");
    }
}