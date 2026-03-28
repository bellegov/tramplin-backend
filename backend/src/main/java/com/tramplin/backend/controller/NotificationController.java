package com.tramplin.backend.controller;

import com.tramplin.backend.dto.NotificationResponse;
import com.tramplin.backend.model.Notification;
import com.tramplin.backend.model.User;
import com.tramplin.backend.repository.NotificationRepository;
import com.tramplin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        User user = getCurrentUser();

        // Превращаем сущности БД в безопасные DTO для фронта
        List<NotificationResponse> responses = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getMessage(),
                        n.isRead(),
                        n.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        User user = getCurrentUser();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        // ПРОВЕРКА БЕЗОПАСНОСТИ: Убеждаемся, что это уведомление принадлежит текущему юзеру!
        if (!n.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Нет прав на изменение чужого уведомления");
        }

        n.setRead(true);
        notificationRepository.save(n);
        return ResponseEntity.ok().build();
    }

    // БОНУС: Ручка чтобы посчитать непрочитанные (для красной точки на колокольчике)
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        User user = getCurrentUser();
        return ResponseEntity.ok(notificationRepository.countByUserIdAndIsReadFalse(user.getId()));
    }
}