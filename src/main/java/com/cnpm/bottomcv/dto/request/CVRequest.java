package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.validation.File.ValidFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CVRequest {

    // userId will be automatically set from authentication context

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "CV file is required")
    @ValidFile(message = "CV file must be a PDF and less than 5MB")
    private MultipartFile cvFile;

    @NotBlank(message = "Skills is required")
    private String skills;

    @NotBlank(message = "Experience is required")
    private String experience;

    @NotBlank(message = "Content is required")
    private String content;
}