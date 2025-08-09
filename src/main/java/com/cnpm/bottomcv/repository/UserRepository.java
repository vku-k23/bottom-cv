package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(@NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") String username);

    @Query("""
        SELECT CASE
            WHEN u.status = 'ACTIVE'
             AND p.email = :email
             AND p.email IS NOT NULL
             AND p.email <> ''
            THEN TRUE
            ELSE FALSE
        END
        FROM User u
        JOIN u.profile p
        WHERE p.email = :email
    """)
    boolean isUserActiveWithEmail(@Param("email") String email);
}