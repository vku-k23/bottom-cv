package com.cnpm.bottomcv.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String resourceType;
    private Long resourceId;
    private String reason;
    private boolean resolved;
}