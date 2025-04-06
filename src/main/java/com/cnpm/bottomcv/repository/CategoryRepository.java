package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsBySlug(String slug);
}
