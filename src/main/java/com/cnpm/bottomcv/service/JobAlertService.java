package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.JobAlertRequest;
import com.cnpm.bottomcv.dto.response.JobAlertResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;

public interface JobAlertService {
    JobAlertResponse createJobAlert(JobAlertRequest request);

    ListResponse<JobAlertResponse> listJobAlerts(int pageNo, int pageSize, String sortBy, String sortType);

    JobAlertResponse updateJobAlert(Long id, JobAlertRequest request);

    void deleteJobAlert(Long id);

    void dispatchAlerts();
}