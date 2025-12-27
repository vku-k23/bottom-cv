package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    Optional<SystemConfig> findFirstByOrderByIdAsc();
}

