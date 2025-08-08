package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.InterviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InterviewResponse {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private LocalDateTime scheduledAt;
    private String location;
    private InterviewStatus status;
}