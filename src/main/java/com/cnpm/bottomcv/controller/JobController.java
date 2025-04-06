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

@Tag(name = "Job API", description = "The API of job")
@RestController
@RequestMapping(value = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // Back APIs (for dashboard - EMPLOYER, ADMIN)
    @PostMapping("/back/jobs")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request) {
        JobResponse response = jobService.createJob(request);
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
    public ResponseEntity<ListResponse<JobResponse>> getAllJobsForBack(@ModelAttribute JobSearchRequest request) {
        ListResponse<JobResponse> response = jobService.getAllJobs(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/back/jobs/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        JobResponse response = jobService.updateJob(id, request);
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
}