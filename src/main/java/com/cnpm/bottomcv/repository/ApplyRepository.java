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
  
  @EntityGraph(attributePaths = {"user", "job"})
  @Query("SELECT a FROM Apply a ORDER BY a.createdAt DESC")
  List<Apply> findTop5ByOrderByCreatedAtDesc();
}
