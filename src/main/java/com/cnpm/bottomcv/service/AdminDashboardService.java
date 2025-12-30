package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.response.ActivityLogResponse;
import com.cnpm.bottomcv.dto.response.AdminStatsResponse;
import com.cnpm.bottomcv.dto.response.ChartDataResponse;

import java.util.List;

public interface AdminDashboardService {
    AdminStatsResponse getStats(org.springframework.security.core.Authentication authentication);

    List<ActivityLogResponse> getAuditLogs();

    void getSystemConfig();

    void updateSystemConfig();
    
    ChartDataResponse getUserGrowthChart(int days);
    
    ChartDataResponse getJobTrendChart(int days);
}