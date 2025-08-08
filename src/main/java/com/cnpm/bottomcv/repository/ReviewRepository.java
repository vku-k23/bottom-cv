package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUser_IdAndCompany_Id(Long userId, Long companyId);
}
