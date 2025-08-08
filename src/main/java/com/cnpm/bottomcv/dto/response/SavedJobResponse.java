package com.cnpm.bottomcv.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavedJobResponse {
    private Long id;
    private Long jobId;
    private Long userId;
}