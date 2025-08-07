package com.cnpm.bottomcv.validation.PastDateOfBirth;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class PastDateOfBirthLocalDateValidator implements ConstraintValidator<PastDateOfBirth, LocalDate> {
    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        if (dob == null) {
            return true; // Let @NotNull handle null validation if needed
        }

        // Date of birth must be before today (in the past)
        return dob.isBefore(LocalDate.now());
    }
}
