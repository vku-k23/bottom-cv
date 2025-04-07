package com.cnpm.bottomcv.repository.ai;

import com.cnpm.bottomcv.model.ai.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    Recommendation findByUserId(Long userId);
}