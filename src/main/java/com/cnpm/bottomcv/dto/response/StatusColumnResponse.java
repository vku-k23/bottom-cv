package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusColumnResponse {
    private Long id;
    private String name;
    private String code;
    private Integer displayOrder;
    private Boolean isDefault;
    private Long jobId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

