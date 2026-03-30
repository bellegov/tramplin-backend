package com.tramplin.backend.repository;

import com.tramplin.backend.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Мои друзья (я отправил или принял, и статус true)
    List<Friendship> findAllBySeekerIdAndAcceptedTrue(Long seekerId);

    // Входящие заявки (отправили мне, статус false)
    List<Friendship> findAllByFriendIdAndAcceptedFalse(Long friendId);

    // Проверка существования связи
    boolean existsBySeekerIdAndFriendId(Long seekerId, Long friendId);

    // Найти конкретную заявку
    Optional<Friendship> findBySeekerIdAndFriendId(Long seekerId, Long friendId);
}