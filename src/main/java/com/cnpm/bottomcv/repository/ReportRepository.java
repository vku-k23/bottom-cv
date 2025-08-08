package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByResolved(boolean resolved);
}