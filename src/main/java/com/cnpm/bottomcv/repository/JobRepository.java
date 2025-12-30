package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.model.Job;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Long countByStatus(StatusJob status);
    
    Long countByCreatedAtAfter(LocalDateTime date);
    
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Long countByCompanyId(Long companyId);
    Long countByCompanyIdAndStatus(Long companyId, StatusJob status);
    Long countByCompanyIdAndCreatedAtAfter(Long companyId, LocalDateTime date);
    Long countByCompanyIdAndCreatedAtBetween(Long companyId, LocalDateTime startDate, LocalDateTime endDate);
    
    @EntityGraph(attributePaths = {"company"})
    @Query("SELECT j FROM Job j ORDER BY j.createdAt DESC")
    List<Job> findTop5ByOrderByCreatedAtDesc();
}
