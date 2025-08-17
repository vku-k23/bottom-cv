package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Apply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplyRepository extends JpaRepository<Apply, Long> {
  Optional<Apply> findByIdAndUserId(Long id, Long userId);

  Page<Apply> findByUserId(Long id, Pageable pageable);

  boolean existsByIdAndUserId(Long id, Long id1);
}
