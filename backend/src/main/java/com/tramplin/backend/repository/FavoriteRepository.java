package com.tramplin.backend.repository;

import com.tramplin.backend.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findAllByUserId(Long userId);
    void deleteByUserIdAndOpportunityId(Long userId, Long opportunityId);
    java.util.Optional<Favorite> findByUserIdAndOpportunityId(Long userId, Long opportunityId);
}