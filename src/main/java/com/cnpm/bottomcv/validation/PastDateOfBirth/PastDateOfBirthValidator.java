package com.cnpm.bottomcv.validation.PastDateOfBirth;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PastDateOfBirthValidator implements ConstraintValidator<PastDateOfBirth, String> {
    @Override
    public boolean isValid(String dobString, ConstraintValidatorContext context) {
        if (dobString == null || dobString.trim().isEmpty()) {
            return true; // Let @NotNull handle null/empty validation if needed
        }

        try {
            LocalDate dob = LocalDate.parse(dobString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            LocalDate today = LocalDate.now();

            // Date of birth must be before today (in the past)
            return dob.isBefore(today);
        } catch (DateTimeParseException e) {
            // Invalid date format
            return false;
        }
    }
}