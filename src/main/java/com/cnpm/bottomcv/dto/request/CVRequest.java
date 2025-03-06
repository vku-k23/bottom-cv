package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "CV file is required")
    private MultipartFile cvFile;
}
