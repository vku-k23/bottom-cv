package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.constant.BlogStatus;
import com.cnpm.bottomcv.model.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long>, JpaSpecificationExecutor<Blog> {

    Optional<Blog> findBySlug(String slug);

    Optional<Blog> findBySlugAndStatus(String slug, BlogStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<Blog> findByStatus(BlogStatus status, Pageable pageable);

    Page<Blog> findByStatusAndCategory_Id(BlogStatus status, Long categoryId, Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE b.status = :status " +
            "AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Blog> searchPublishedBlogs(@Param("keyword") String keyword, @Param("status") BlogStatus status, Pageable pageable);

    List<Blog> findTop5ByStatusOrderByPublishedAtDesc(BlogStatus status);

    List<Blog> findTop3ByStatusAndIdNotOrderByPublishedAtDesc(BlogStatus status, Long id);

    Long countByStatus(BlogStatus status);

    @Modifying
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void incrementViewCount(@Param("id") Long id);
}


