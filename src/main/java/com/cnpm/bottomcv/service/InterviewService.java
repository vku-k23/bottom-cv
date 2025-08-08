package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.InterviewRequest;
import com.cnpm.bottomcv.dto.response.InterviewResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;

public interface InterviewService {
    InterviewResponse createInterview(InterviewRequest request);

    ListResponse<InterviewResponse> listInterviews(Long jobId, int pageNo, int pageSize);

    InterviewResponse updateInterview(Long interviewId, InterviewRequest request);

    void deleteInterview(Long interviewId);

    void confirmInterview(Long interviewId);

    void declineInterview(Long interviewId);
}