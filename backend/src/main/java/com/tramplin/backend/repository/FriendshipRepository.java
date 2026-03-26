package com.tramplin.backend.repository;


import com.tramplin.backend.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findAllBySeekerIdAndAcceptedTrue(Long seekerId);
    boolean existsBySeekerIdAndFriendId(Long seekerId, Long friendId);
}