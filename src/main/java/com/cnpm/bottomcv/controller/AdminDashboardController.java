package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Dashboard API", description = "Skeleton endpoints for admin dashboard and system config")
@RestController
@RequestMapping(value = "/api/v1/back/admin", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> getStats() {
        adminDashboardService.getStats();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> getAuditLogs() {
        adminDashboardService.getAuditLogs();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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