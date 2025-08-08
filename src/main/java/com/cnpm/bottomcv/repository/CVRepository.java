package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.CV;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    List<CV> findByUserId(Long id);

    Page<CV> findAllByUserUsername(String userUsername, Pageable pageable);
}
