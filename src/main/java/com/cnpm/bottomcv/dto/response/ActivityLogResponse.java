package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogResponse {
    private Long id;
    private String activityType; // USER_REGISTRATION, JOB_POSTED, APPLICATION_SUBMITTED, etc.
    private String message;
    private String userName;
    private Long userId;
    private String status; // success, warning, info, error
    private String timestamp;
    private String resourceType; // USER, JOB, APPLICATION, COMPANY
    private Long resourceId;
}

