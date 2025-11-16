package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.request.BulkModerationRequest;
import com.cnpm.bottomcv.dto.request.ModerationRequest;
import com.cnpm.bottomcv.dto.response.JobResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ModerationQueueResponse;

public interface ModerationService {
    ListResponse<ModerationQueueResponse> getModerationQueue(StatusJob status, int pageNo, int pageSize);
    
    JobResponse approveJob(Long jobId, ModerationRequest request);

    JobResponse rejectJob(Long jobId, ModerationRequest request);
    
    void bulkApproveJobs(BulkModerationRequest request);
    
    void bulkRejectJobs(BulkModerationRequest request);

    void approveReview(Long reviewId);

    void rejectReview(Long reviewId, String reason);
}