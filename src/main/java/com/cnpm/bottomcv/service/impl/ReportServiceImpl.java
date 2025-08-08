package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.ReportRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ReportResponse;
import com.cnpm.bottomcv.service.ReportService;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {
    @Override
    public ReportResponse createReport(ReportRequest request) {
        return ReportResponse.builder().build();
    }

    @Override
    public ListResponse<ReportResponse> listReports(Boolean resolved, int pageNo, int pageSize) {
        return ListResponse.<ReportResponse>builder().build();
    }

    @Override
    public ReportResponse resolveReport(Long reportId) {
        return ReportResponse.builder().build();
    }
}