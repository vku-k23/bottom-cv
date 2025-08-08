package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotBlank
    private String resourceType;

    @NotNull
    private Long resourceId;

    @NotBlank
    private String reason;
}