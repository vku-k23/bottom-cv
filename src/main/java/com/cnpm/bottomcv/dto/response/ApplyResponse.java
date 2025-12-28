package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.StatusJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplyResponse {
    private Long id;
    private Long userId;
    private Long jobId;
    private Long cvId;
    private String cvUrl;
    private String coverLetter;
    private StatusJob status;
    private String message;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}