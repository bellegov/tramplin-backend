package com.tramplin.backend.service;

import com.tramplin.backend.dto.SeekerProfileResponse;
import com.tramplin.backend.model.*;
import com.tramplin.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NetworkingService {
    private final SeekerProfileRepository seekerRepo;
    private final FriendshipRepository friendshipRepo;
    private final RecommendationRepository recommendationRepo;
    private final UserRepository userRepo;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email).orElseThrow();
    }

    private SeekerProfile getCurrentSeeker() {
        return seekerRepo.findById(getCurrentUser().getId()).orElseThrow();
    }

    // Добавить друга
    public void addFriend(Long friendId) {
        SeekerProfile me = getCurrentSeeker();
        SeekerProfile friend = seekerRepo.findById(friendId).orElseThrow();

        if (!friendshipRepo.existsBySeekerIdAndFriendId(me.getId(), friend.getId())) {
            friendshipRepo.save(Friendship.builder()
                    .seeker(me)
                    .friend(friend)
                    .accepted(true)
                    .build());
        }
    }

    // Получить список друзей
    public List<SeekerProfileResponse> getMyFriends() {
        SeekerProfile me = getCurrentSeeker();
        return friendshipRepo.findAllBySeekerIdAndAcceptedTrue(me.getId())
                .stream()
                .map(f -> mapToResponse(f.getFriend()))
                .collect(Collectors.toList());
    }

    // Рекомендовать друга
    public void recommendFriend(Long friendId, Long opportunityId, String msg) {
        SeekerProfile me = getCurrentSeeker();
        SeekerProfile friend = seekerRepo.findById(friendId).orElseThrow();

        recommendationRepo.save(Recommendation.builder()
                .recommender(me)
                .recommended(friend)
                .opportunity(Opportunity.builder().id(opportunityId).build())
                .message(msg)
                .build());
    }

    // Поиск людей (только публичные профили)
    public List<SeekerProfileResponse> searchPeople(String name) {
        return seekerRepo.findAll().stream()
                .filter(p -> p.isPublic() && p.getUser().getDisplayName().toLowerCase().contains(name.toLowerCase()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 2. Принять запрос в друзья
    @Transactional
    public void acceptFriendRequest(Long seekerIdWhoSentRequest) {
        SeekerProfile me = getCurrentSeeker(); // Я (тот, кому отправили)

        // Ищем заявку, где seeker - это тот парень, а friend - это Я
        Friendship request = friendshipRepo.findBySeekerIdAndFriendId(seekerIdWhoSentRequest, me.getId())
                .orElseThrow(() -> new RuntimeException("Запрос в друзья не найден"));

        request.setAccepted(true); // Приняли!
        friendshipRepo.save(request);

        // Создаем обратную связь для удобства поиска (Я -> Тот парень = true)
        if (!friendshipRepo.existsBySeekerIdAndFriendId(me.getId(), seekerIdWhoSentRequest)) {
            friendshipRepo.save(Friendship.builder()
                    .seeker(me)
                    .friend(request.getSeeker())
                    .accepted(true)
                    .build());
        }
    }

    // 3. Получить список ВХОДЯЩИХ заявок (кто хочет дружить со мной)
    public List<SeekerProfileResponse> getIncomingRequests() {
        SeekerProfile me = getCurrentSeeker();

        // Ищем, где friend_id = Я, а accepted = false
        return friendshipRepo.findAllByFriendIdAndAcceptedFalse(me.getId())
                .stream()
                .map(f -> mapToResponse(f.getSeeker())) // Показываем тех, кто отправил
                .collect(Collectors.toList());
    }



    private SeekerProfileResponse mapToResponse(SeekerProfile p) {
        return new SeekerProfileResponse(
                p.getId(), p.getUser().getDisplayName(), p.getUser().getEmail(),
                p.getUniversity(), p.getGraduationYear(), p.getResumeText(),
                p.getPortfolioLinks(), p.isPublic(), p.getAvatarUrl());
    }
}