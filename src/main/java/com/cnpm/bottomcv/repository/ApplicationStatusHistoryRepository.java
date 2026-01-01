package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.constant.ApplicationStatus;
import com.cnpm.bottomcv.model.ApplicationStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {
    
    List<ApplicationStatusHistory> findByApplicationIdOrderByChangedAtDesc(Long applicationId);
    
    Page<ApplicationStatusHistory> findByApplicationId(Long applicationId, Pageable pageable);
    
    Optional<ApplicationStatusHistory> findTopByApplicationIdOrderByChangedAtDesc(Long applicationId);
    
    @Query("SELECT h FROM ApplicationStatusHistory h WHERE h.application.id = :applicationId AND h.newStatus = :status ORDER BY h.changedAt DESC")
    Optional<ApplicationStatusHistory> findLatestByApplicationIdAndStatus(
            @Param("applicationId") Long applicationId,
            @Param("status") ApplicationStatus status);
    
    Long countByNewStatus(ApplicationStatus status);
    
    @Query("SELECT h FROM ApplicationStatusHistory h WHERE h.changedBy.id = :userId ORDER BY h.changedAt DESC")
    Page<ApplicationStatusHistory> findByChangedById(@Param("userId") Long userId, Pageable pageable);
}

