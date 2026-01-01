package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateStatusColumnRequest {
    @NotBlank(message = "Column name is required")
    @Size(min = 1, max = 100, message = "Column name must be between 1 and 100 characters")
    private String name;

    private Long jobId; // Optional: if provided, column is job-specific; if null, it's global
}

