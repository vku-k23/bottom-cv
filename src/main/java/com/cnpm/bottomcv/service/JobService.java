package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.JobRequest;
import com.cnpm.bottomcv.dto.response.JobResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;

public interface JobService {
    JobResponse createJob(JobRequest request);

    JobResponse getJobById(Long id);

    ListResponse<JobResponse> getAllJobs(int pageNo, int pageSize, String sortBy, String sortType);

    JobResponse updateJob(Long id, JobRequest request);

    void deleteJob(Long id);
}
