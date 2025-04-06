package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.validation.ValidFile;
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

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "CV file is required")
    @ValidFile(allowedTypes = {"application/pdf"}, maxSize = 5 * 1024 * 1024, message = "CV file must be a PDF and less than 5MB")
    private MultipartFile cvFile;
}