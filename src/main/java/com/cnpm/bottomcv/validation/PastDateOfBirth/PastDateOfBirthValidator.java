package com.cnpm.bottomcv.validation.PastDateOfBirth;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PastDateOfBirthValidator implements ConstraintValidator<PastDateOfBirth, String> {
    @Override
    public boolean isValid(String dobString, ConstraintValidatorContext context) {
        if (dobString == null || dobString.trim().isEmpty()) {
            return true;
        }
        try {
            LocalDateTime dob = LocalDateTime.parse(dobString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            return dob.isBefore(LocalDateTime.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}