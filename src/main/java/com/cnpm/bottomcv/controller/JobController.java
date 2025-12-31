package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.JobRequest;
import com.cnpm.bottomcv.dto.request.JobSearchRequest;
import com.cnpm.bottomcv.dto.response.JobResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Job API", description = "The API of job")
@RestController
@RequestMapping(value = "/api/v1", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // Back APIs (for dashboard - EMPLOYER, ADMIN)
    @PostMapping("/back/jobs")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request,
            org.springframework.security.core.Authentication authentication) {
        JobResponse response = jobService.createJob(request, authentication);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/back/jobs/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<JobResponse> getJobByIdForBack(@PathVariable Long id) {
        JobResponse response = jobService.getJobById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/back/jobs")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ListResponse<JobResponse>> getAllJobsForBack(
            @ModelAttribute com.cnpm.bottomcv.dto.request.JobFilterRequest filterRequest) {
        // Convert JobFilterRequest to JobSearchRequest
        JobSearchRequest request = new JobSearchRequest();
        request.setKeyword(filterRequest.getSearch());
        request.setLocation(filterRequest.getLocation());
        if (filterRequest.getJobType() != null) {
            try {
                request.setJobType(com.cnpm.bottomcv.constant.JobType.valueOf(filterRequest.getJobType()));
            } catch (IllegalArgumentException e) {
                // Invalid job type, ignore
            }
        }
        if (filterRequest.getStatus() != null) {
            try {
                request.setStatus(com.cnpm.bottomcv.constant.StatusJob.valueOf(filterRequest.getStatus()));
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        request.setCategoryId(filterRequest.getCategoryId());
        request.setCompanyId(filterRequest.getCompanyId());
        request.setSortBy(filterRequest.getSortBy());
        request.setSortDirection(filterRequest.getSortType());
        request.setPage(filterRequest.getPageNo() != null ? filterRequest.getPageNo() : 0);
        request.setSize(filterRequest.getPageSize() != null ? filterRequest.getPageSize() : 10);
        
        ListResponse<JobResponse> response = jobService.getAllJobs(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/back/jobs/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            org.springframework.security.core.Authentication authentication) {
        JobResponse response = jobService.updateJob(id, request, authentication);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/back/jobs/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    // Front APIs (for client web - public or CANDIDATE)
    @GetMapping("/front/jobs/{id}")
    public ResponseEntity<JobResponse> getJobByIdForFront(@PathVariable Long id) {
        JobResponse response = jobService.getJobById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/front/jobs")
    public ResponseEntity<ListResponse<JobResponse>> getAllJobsForFront(@ModelAttribute JobSearchRequest request) {
        ListResponse<JobResponse> response = jobService.getAllJobs(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/front/jobs/recommended/request")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> requestRecommendedJobs(@RequestParam Long userId) {
        jobService.requestRecommendation(userId);
        return ResponseEntity.ok("Recommendation request submitted for user: " + userId);
    }

    @GetMapping("/front/jobs/recommended/result")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ListResponse<JobResponse>> getRecommendedJobs(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) throws IOException {
        ListResponse<JobResponse> response = jobService.getRecommendedJobs(userId, pageNo, pageSize);
        return ResponseEntity.ok(response);
    }
}