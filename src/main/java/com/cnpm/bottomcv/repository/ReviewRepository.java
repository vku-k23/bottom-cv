package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
