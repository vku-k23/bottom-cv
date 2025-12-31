package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.request.ApplyRequest;
import com.cnpm.bottomcv.dto.request.UpdateApplicationStatusRequest;
import com.cnpm.bottomcv.dto.response.ApplyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.ApplyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Apply job API", description = "The API of apply job")
@RestController
@RequestMapping(value = "/api/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;

    @PostMapping(value = "/applies/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplyResponse> submitApplication(
            @RequestParam("jobId") Long jobId,
            @RequestParam("coverLetter") String coverLetter,
            @RequestPart("cvFile") MultipartFile cvFile,
            Authentication authentication) {
        ApplyResponse response = applyService.submitApplication(jobId, coverLetter, cvFile, authentication);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/applies/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApplyResponse> getApplyById(@PathVariable Long id, Authentication authentication) {
        ApplyResponse response = applyService.getApplyById(id, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applies")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER', 'ADMIN')")
    public ResponseEntity<ListResponse<ApplyResponse>> getAllApplies(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType,
            Authentication authentication) {
        ListResponse<ApplyResponse> responses = applyService.getAllApplies(pageNo, pageSize, sortBy, sortType, authentication);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/applies")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplyResponse> createApply(@Valid @RequestBody ApplyRequest request, Authentication authentication) {
        ApplyResponse response = applyService.createApply(request, authentication);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/applies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApplyResponse> updateApply(@PathVariable Long id, @Valid @RequestBody ApplyRequest request, Authentication authentication) {
        ApplyResponse response = applyService.updateApply(id, request, authentication);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/applies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Void> deleteApply(@PathVariable Long id, Authentication authentication) {
        applyService.deleteApply(id, authentication);
        return ResponseEntity.noContent().build();
    }

    // New endpoints for Kanban board functionality

    @GetMapping("/applies/job/{jobId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ListResponse<ApplyResponse>> getAppliesByJobId(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortType,
            Authentication authentication) {
        ListResponse<ApplyResponse> responses = applyService.getAppliesByJobId(jobId, pageNo, pageSize, sortBy, sortType, authentication);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/applies/job/{jobId}/status/{status}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ListResponse<ApplyResponse>> getAppliesByJobIdAndStatus(
            @PathVariable Long jobId,
            @PathVariable StatusJob status,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortType,
            Authentication authentication) {
        ListResponse<ApplyResponse> responses = applyService.getAppliesByJobIdAndStatus(jobId, status, pageNo, pageSize, sortBy, sortType, authentication);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/applies/job/{jobId}/grouped-by-status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<java.util.Map<StatusJob, java.util.List<ApplyResponse>>> getAppliesGroupedByStatus(
            @PathVariable Long jobId,
            Authentication authentication) {
        java.util.Map<StatusJob, java.util.List<ApplyResponse>> grouped = applyService.getAppliesGroupedByStatus(jobId, authentication);
        return ResponseEntity.ok(grouped);
    }

    @PutMapping("/applies/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApplyResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStatusRequest request,
            Authentication authentication) {
        ApplyResponse response = applyService.updateApplicationStatus(id, request, authentication);
        return ResponseEntity.ok(response);
    }
}
