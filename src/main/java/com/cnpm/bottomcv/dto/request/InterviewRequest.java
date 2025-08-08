package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewRequest {
    @NotNull
    private Long jobId;

    @NotNull
    private Long candidateId;

    @NotNull
    @Future
    private LocalDateTime scheduledAt;

    private String location;
}