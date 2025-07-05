package com.cnpm.bottomcv.validation.InvalidWords;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InvalidWordsValidator.class)
public @interface InvalidWords {
    String message() default "Your language violates the rules of the system";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}