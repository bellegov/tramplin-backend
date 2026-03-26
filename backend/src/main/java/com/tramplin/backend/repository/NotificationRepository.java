package com.tramplin.backend.repository;

import com.tramplin.backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Получить уведомления пользователя, самые свежие — сверху
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // Посчитать количество непрочитанных (для красной точки на колокольчике)
    long countByUserIdAndIsReadFalse(Long userId);
}