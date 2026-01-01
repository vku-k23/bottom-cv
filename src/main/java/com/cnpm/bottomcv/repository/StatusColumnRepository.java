package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.StatusColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusColumnRepository extends JpaRepository<StatusColumn, Long> {
    Optional<StatusColumn> findByCode(String code);

    List<StatusColumn> findByJobIdOrderByDisplayOrderAsc(Long jobId);

    List<StatusColumn> findByJobIdIsNullOrderByDisplayOrderAsc(); // Global columns

    @Query("SELECT sc FROM StatusColumn sc WHERE (sc.job.id = :jobId OR sc.job IS NULL) ORDER BY sc.displayOrder ASC")
    List<StatusColumn> findByJobIdOrGlobalOrderByDisplayOrderAsc(Long jobId);
    
    // Always get global columns (default columns) regardless of jobId
    @Query("SELECT sc FROM StatusColumn sc WHERE sc.job IS NULL ORDER BY sc.displayOrder ASC")
    List<StatusColumn> findAllGlobalColumnsOrderByDisplayOrderAsc();

    boolean existsByCode(String code);

    boolean existsByNameAndJobId(String name, Long jobId);

    boolean existsByNameAndJobIdIsNull(String name);
}

