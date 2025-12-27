package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkModerationRequest {
    @NotEmpty(message = "Job IDs cannot be empty")
    private List<Long> jobIds;
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}

