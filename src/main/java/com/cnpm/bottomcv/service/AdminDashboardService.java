package com.cnpm.bottomcv.service;

public interface AdminDashboardService {
    void getStats();

    void getAuditLogs();

    void getSystemConfig();

    void updateSystemConfig();
}