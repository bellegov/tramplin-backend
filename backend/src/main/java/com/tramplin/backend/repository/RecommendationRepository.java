package com.tramplin.backend.repository;

import com.tramplin.backend.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findAllByRecommendedId(Long recommendedId);
}