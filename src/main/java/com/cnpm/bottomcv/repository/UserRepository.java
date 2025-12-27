package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(@NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username);

    @Query("""
                SELECT CASE WHEN u.status = 'ACTIVE' THEN TRUE ELSE FALSE END
                FROM User u
                JOIN u.profile p
                WHERE p.email = :email
            """)
    boolean isUserActiveWithEmail(@Param("email") String email);

    @Query("""
                SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END
                FROM User u
                JOIN u.profile p
                WHERE p.email = :email
            """)
    boolean existsByEmail(@Param("email") String email);

    @Query("""
                SELECT u
                FROM User u
                JOIN u.profile p
                WHERE p.email = :email
            """)
    Optional<User> findByEmail(@Param("email") String email);
    
    // Admin dashboard statistics methods
    Long countByRoles_Name(RoleType roleType);
    
    Long countByCreatedAtAfter(LocalDateTime date);
    
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<User> findTop5ByOrderByCreatedAtDesc();
}