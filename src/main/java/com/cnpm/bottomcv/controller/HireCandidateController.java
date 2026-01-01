package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.HireCandidateRequest;
import com.cnpm.bottomcv.dto.response.ApplicationStatusHistoryResponse;
import com.cnpm.bottomcv.dto.response.HireCandidateResponse;
import com.cnpm.bottomcv.service.HireCandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/admin/hiring", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@Tag(name = "Hiring Management", description = "APIs for hiring candidates and managing application status")
@Slf4j
public class HireCandidateController {

    private final HireCandidateService hireCandidateService;

    @PostMapping("/hire")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Hire a candidate", description = "Hire a candidate by updating their application status to HIRED")
    public ResponseEntity<HireCandidateResponse> hireCandidate(
            @Valid @RequestBody HireCandidateRequest request,
            Authentication authentication) {
        log.info("Hiring candidate for application {} by user: {}", request.getApplicationId(), authentication.getName());
        HireCandidateResponse response = hireCandidateService.hireCandidate(request, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applications/{applicationId}/history")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get application status history", description = "Get the status change history for an application")
    public ResponseEntity<List<ApplicationStatusHistoryResponse>> getApplicationStatusHistory(
            @PathVariable Long applicationId,
            Authentication authentication) {
        log.info("Fetching status history for application {} by user: {}", applicationId, authentication.getName());
        List<ApplicationStatusHistoryResponse> history = hireCandidateService.getApplicationStatusHistory(applicationId, authentication);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Update application status", description = "Update the status of an application with history tracking")
    public ResponseEntity<ApplicationStatusHistoryResponse> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam String status,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        log.info("Updating status of application {} to {} by user: {}", applicationId, status, authentication.getName());
        ApplicationStatusHistoryResponse response = hireCandidateService.updateApplicationStatus(applicationId, status, note, authentication);
        return ResponseEntity.ok(response);
    }
}

