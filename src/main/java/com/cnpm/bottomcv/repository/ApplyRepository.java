package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Apply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, Long> {
  Optional<Apply> findByIdAndUserId(Long id, Long userId);

  Page<Apply> findByUserId(Long id, Pageable pageable);

  boolean existsByIdAndUserId(Long id, Long id1);
  
  Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
  
      Long countByCreatedAtAfter(LocalDateTime date);
  
      Long countByJob_CompanyId(Long companyId);
      Long countByJob_CompanyIdAndCreatedAtAfter(Long companyId, LocalDateTime date);
      Long countByJob_CompanyIdAndCreatedAtBetween(Long companyId, LocalDateTime startDate, LocalDateTime endDate);
      
      @EntityGraph(attributePaths = {"user", "job"})
      @Query("SELECT a FROM Apply a ORDER BY a.createdAt DESC")
      List<Apply> findTop5ByOrderByCreatedAtDesc();
      
  // New methods for Kanban board functionality
  Page<Apply> findByJob_CompanyId(Long companyId, Pageable pageable);
  
  Page<Apply> findByJobId(Long jobId, Pageable pageable);
  
  Page<Apply> findByJob_CompanyIdAndStatus(Long companyId, com.cnpm.bottomcv.constant.StatusJob status, Pageable pageable);
  
  Page<Apply> findByJobIdAndStatus(Long jobId, com.cnpm.bottomcv.constant.StatusJob status, Pageable pageable);
  
  List<Apply> findByJob_CompanyId(Long companyId);
  
  List<Apply> findByJobId(Long jobId);
  
  @EntityGraph(attributePaths = {"user", "job", "cv"})
  List<Apply> findByJob_CompanyIdOrderByCreatedAtDesc(Long companyId);
  
  @EntityGraph(attributePaths = {"user", "job", "cv"})
  List<Apply> findByJobIdOrderByCreatedAtDesc(Long jobId);
}
