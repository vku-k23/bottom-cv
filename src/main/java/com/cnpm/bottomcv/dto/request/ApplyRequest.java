package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.StatusJob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplyRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "CV ID is required")
    private Long cvId;

    @NotNull(message = "Status is required")
    private StatusJob status;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must be less than 500 characters")
    private String message;
}