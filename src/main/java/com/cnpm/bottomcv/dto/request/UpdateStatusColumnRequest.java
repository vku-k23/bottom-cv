package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateStatusColumnRequest {
    @Size(min = 1, max = 100, message = "Column name must be between 1 and 100 characters")
    private String name;

    private Integer displayOrder; // Optional: update display order
}

