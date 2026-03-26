package com.tramplin.backend.repository;

import com.tramplin.backend.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    // Найти всех подписчиков конкретной компании (нужно для рассылки уведомлений)
    List<Subscription> findAllByEmployerId(Long employerId);

    // Проверить, подписан ли уже студент на эту компанию
    boolean existsBySeekerIdAndEmployerId(Long seekerId, Long employerId);

    // Найти подписку, чтобы удалить её (отписаться)
    Optional<Subscription> findBySeekerIdAndEmployerId(Long seekerId, Long employerId);
}