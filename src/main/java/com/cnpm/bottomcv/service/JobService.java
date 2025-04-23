package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.JobRequest;
import com.cnpm.bottomcv.dto.request.JobSearchRequest;
import com.cnpm.bottomcv.dto.response.JobResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;

import java.io.IOException;

public interface JobService {
    JobResponse createJob(JobRequest request);

    JobResponse getJobById(Long id);

    ListResponse<JobResponse> getAllJobs(JobSearchRequest jobSearchRequest);

    JobResponse updateJob(Long id, JobRequest request);

    void deleteJob(Long id);

    void requestRecommendation(Long userId);

    ListResponse<JobResponse> getRecommendedJobs(Long userId, int pageNo, int pageSize) throws IOException;
}
