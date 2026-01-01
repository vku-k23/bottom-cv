package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationStatusHistoryResponse {
    
    private Long id;
    private Long applicationId;
    private ApplicationStatus previousStatus;
    private ApplicationStatus newStatus;
    private Long changedById;
    private String changedByName;
    private LocalDateTime changedAt;
    private String note;
    private String offerDetails;
}

