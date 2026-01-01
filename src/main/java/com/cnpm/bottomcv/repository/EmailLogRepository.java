package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.constant.EmailStatus;
import com.cnpm.bottomcv.model.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    Page<EmailLog> findBySenderId(Long senderId, Pageable pageable);

    Page<EmailLog> findByCandidateId(Long candidateId, Pageable pageable);

    Page<EmailLog> findByApplicationId(Long applicationId, Pageable pageable);

    List<EmailLog> findByStatusAndRetryCountLessThan(EmailStatus status, Integer maxRetries);

    @Query("SELECT e FROM EmailLog e WHERE e.sender.id = :senderId AND e.createdAt BETWEEN :startDate AND :endDate")
    Page<EmailLog> findBySenderIdAndDateRange(
            @Param("senderId") Long senderId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    Long countBySenderIdAndStatus(Long senderId, EmailStatus status);

    @Query("SELECT e FROM EmailLog e WHERE e.job.company.id = :companyId ORDER BY e.createdAt DESC")
    Page<EmailLog> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
