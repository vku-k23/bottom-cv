package com.cnpm.bottomcv.validation.PastDateOfBirth;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class PastDateOfBirthLocalDateTimeValidator implements ConstraintValidator<PastDateOfBirth, LocalDateTime> {
    @Override
    public boolean isValid(LocalDateTime dob, ConstraintValidatorContext context) {
        if (dob == null) {
            return true; // Let @NotNull handle null validation if needed
        }

        // Date of birth must be before now (in the past)
        return dob.isBefore(LocalDateTime.now());
    }
}
