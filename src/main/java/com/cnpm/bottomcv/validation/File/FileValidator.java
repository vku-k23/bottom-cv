package com.cnpm.bottomcv.validation.File;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private String[] allowedTypes;
    private long maxSize;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.allowedTypes = constraintAnnotation.allowedTypes();
        this.maxSize = constraintAnnotation.maxSize();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File size exceeds the maximum limit of " + (maxSize / (1024 * 1024)) + "MB")
                    .addConstraintViolation();
            return false;
        }

        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : allowedTypes) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File type not allowed. Allowed types: " + String.join(", ", allowedTypes))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}