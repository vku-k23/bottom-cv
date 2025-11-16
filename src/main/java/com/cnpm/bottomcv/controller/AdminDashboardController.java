package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.response.ActivityLogResponse;
import com.cnpm.bottomcv.dto.response.AdminStatsResponse;
import com.cnpm.bottomcv.dto.response.ChartDataResponse;
import com.cnpm.bottomcv.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Dashboard API", description = "API for admin dashboard statistics and audit logs")
@RestController
@RequestMapping(value = "/api/v1/back/admin", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatsResponse> getStats() {
        AdminStatsResponse stats = adminDashboardService.getStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/activities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getActivities() {
        List<ActivityLogResponse> activities = adminDashboardService.getAuditLogs();
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/charts/user-growth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChartDataResponse> getUserGrowthChart(
            @RequestParam(defaultValue = "30") int days) {
        ChartDataResponse chart = adminDashboardService.getUserGrowthChart(days);
        return ResponseEntity.ok(chart);
    }

    @GetMapping("/charts/job-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChartDataResponse> getJobTrendChart(
            @RequestParam(defaultValue = "30") int days) {
        ChartDataResponse chart = adminDashboardService.getJobTrendChart(days);
        return ResponseEntity.ok(chart);
    }

    @GetMapping("/system/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> getSystemConfig() {
        adminDashboardService.getSystemConfig();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PutMapping("/system/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateSystemConfig() {
        adminDashboardService.updateSystemConfig();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}