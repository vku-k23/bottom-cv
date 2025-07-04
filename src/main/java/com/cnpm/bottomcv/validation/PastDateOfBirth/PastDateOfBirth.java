package com.cnpm.bottomcv.validation.PastDateOfBirth;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PastDateOfBirthValidator.class)
public @interface PastDateOfBirth {
    String message() default "Date of birth must be in the past.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}