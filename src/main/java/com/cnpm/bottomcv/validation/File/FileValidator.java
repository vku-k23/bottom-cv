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
        // Allow null files - let @NotNull handle null validation
        if (file == null) {
            return true;
        }

        // Check if file is empty
        if (file.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File cannot be empty")
                    .addConstraintViolation();
            return false;
        }

        // Check file size
        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File size exceeds the maximum limit of " + (maxSize / (1024 * 1024)) + "MB")
                    .addConstraintViolation();
            return false;
        }

        // Check file type
        if (!isValidFileType(file)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File type not allowed. Only PDF files are accepted")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isValidFileType(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        // Check content type
        if (contentType != null) {
            for (String allowedType : allowedTypes) {
                if (allowedType.equalsIgnoreCase(contentType)) {
                    return true;
                }
            }
        }

        // Fallback: check file extension if content type is not reliable
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf")) {
            // Additional check: verify it's actually a PDF by checking magic bytes
            return isPdfFile(file);
        }

        return false;
    }

    private boolean isPdfFile(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 4) {
                return false;
            }

            // Check PDF magic bytes: %PDF
            return bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46;
        } catch (Exception e) {
            return false;
        }
    }
}