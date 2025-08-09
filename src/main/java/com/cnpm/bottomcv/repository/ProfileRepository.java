package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.constant.PatternField;
import com.cnpm.bottomcv.model.Profile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email);

    Optional<Profile> findByUserId(Long id);

    boolean existsByPhoneNumber(@NotBlank(message = "Phone number is required")
                                @Pattern(regexp = PatternField.PHONE_NUMBER_PATTERN,
                                        message = "Phone number must contain only digits and be between 10-15 characters")
                                String phoneNumber);

    Optional<Profile> findByEmail(String email);
}
