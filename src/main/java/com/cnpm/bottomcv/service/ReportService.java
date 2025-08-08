package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.ReportRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ReportResponse;

public interface ReportService {
    ReportResponse createReport(ReportRequest request);

    ListResponse<ReportResponse> listReports(Boolean resolved, int pageNo, int pageSize);

    ReportResponse resolveReport(Long reportId);
}