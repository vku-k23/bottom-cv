package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsBySlug(String slug);
}
