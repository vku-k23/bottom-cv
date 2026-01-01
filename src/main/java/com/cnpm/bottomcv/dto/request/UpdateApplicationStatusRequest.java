package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.StatusJob;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateApplicationStatusRequest {
    @NotNull(message = "Status is required")
    private StatusJob status;
    
    private Integer position; // Optional: position within the status column (0-based)
}

