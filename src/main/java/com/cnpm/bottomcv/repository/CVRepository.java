package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.CV;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CVRepository extends JpaRepository<CV, Long> {
}
