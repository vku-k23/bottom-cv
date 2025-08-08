package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.InterviewRequest;
import com.cnpm.bottomcv.dto.response.InterviewResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.InterviewService;
import org.springframework.stereotype.Service;

@Service
public class InterviewServiceImpl implements InterviewService {
    @Override
    public InterviewResponse createInterview(InterviewRequest request) {
        return InterviewResponse.builder().build();
    }

    @Override
    public ListResponse<InterviewResponse> listInterviews(Long jobId, int pageNo, int pageSize) {
        return ListResponse.<InterviewResponse>builder().build();
    }

    @Override
    public InterviewResponse updateInterview(Long interviewId, InterviewRequest request) {
        return InterviewResponse.builder().build();
    }

    @Override
    public void deleteInterview(Long interviewId) {
        // TODO: implement delete interview
    }

    @Override
    public void confirmInterview(Long interviewId) {
        // TODO: implement confirm interview
    }

    @Override
    public void declineInterview(Long interviewId) {
        // TODO: implement decline interview
    }
}