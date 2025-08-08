package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.JobAlertRequest;
import com.cnpm.bottomcv.dto.response.JobAlertResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.JobAlertService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Job Alerts API", description = "Skeleton endpoints for managing job alerts")
@RestController
@RequestMapping(value = "/api/v1", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class JobAlertController {

    private final JobAlertService jobAlertService;

    // Front (candidate)
    @PostMapping("/front/job-alerts")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<JobAlertResponse> createJobAlert(@Valid @RequestBody JobAlertRequest request) {
        var response = jobAlertService.createJobAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/front/job-alerts")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ListResponse<JobAlertResponse>> listJobAlerts(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortType) {
        var response = jobAlertService.listJobAlerts(pageNo, pageSize, sortBy, sortType);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/front/job-alerts/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<JobAlertResponse> updateJobAlert(@PathVariable Long id,
            @Valid @RequestBody JobAlertRequest request) {
        var response = jobAlertService.updateJobAlert(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/front/job-alerts/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> deleteJobAlert(@PathVariable Long id) {
        jobAlertService.deleteJobAlert(id);
        return ResponseEntity.noContent().build();
    }

    // Back (admin)
    @PostMapping("/back/job-alerts/dispatch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> dispatchAlerts() {
        jobAlertService.dispatchAlerts();
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}