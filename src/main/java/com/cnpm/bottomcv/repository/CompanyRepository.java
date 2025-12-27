package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {
    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.jobs WHERE c.id = :id")
    Optional<Company> findByIdWithJobs(@Param("id") Long id);
}
