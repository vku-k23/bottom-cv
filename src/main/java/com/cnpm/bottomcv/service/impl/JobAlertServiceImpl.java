package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.JobAlertRequest;
import com.cnpm.bottomcv.dto.response.JobAlertResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.JobAlertService;
import org.springframework.stereotype.Service;

@Service
public class JobAlertServiceImpl implements JobAlertService {
    @Override
    public JobAlertResponse createJobAlert(JobAlertRequest request) {
        return JobAlertResponse.builder().build();
    }

    @Override
    public ListResponse<JobAlertResponse> listJobAlerts(int pageNo, int pageSize, String sortBy, String sortType) {
        return ListResponse.<JobAlertResponse>builder().build();
    }

    @Override
    public JobAlertResponse updateJobAlert(Long id, JobAlertRequest request) {
        return JobAlertResponse.builder().build();
    }

    @Override
    public void deleteJobAlert(Long id) {
        // TODO: implement delete job alert
    }

    @Override
    public void dispatchAlerts() {
        // TODO: implement dispatch alerts
    }
}