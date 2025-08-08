package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SavedJobRequest {
    @NotNull
    private Long jobId;
}