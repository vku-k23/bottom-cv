package com.cnpm.bottomcv.service;

public interface ModerationService {
    void approveJob(Long jobId);

    void rejectJob(Long jobId, String reason);

    void approveReview(Long reviewId);

    void rejectReview(Long reviewId, String reason);
}