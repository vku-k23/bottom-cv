package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyRequest {

    @NotBlank(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Job id is required")
    private Long jobId;

    @NotBlank(message = "Cv id is required")
    private Long cvId;

    private String message;
}
