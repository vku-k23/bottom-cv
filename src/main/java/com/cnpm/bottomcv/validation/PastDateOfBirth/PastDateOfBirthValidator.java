package com.cnpm.bottomcv.validation.PastDateOfBirth;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PastDateOfBirthValidator implements ConstraintValidator<PastDateOfBirth, String> {
    @Override
    public boolean isValid(String dobString, ConstraintValidatorContext context) {
        if (dobString == null || dobString.trim().isEmpty()) {
            return true;
        }
        try {
            LocalDate dob = LocalDate.parse(dobString);
            return dob.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}