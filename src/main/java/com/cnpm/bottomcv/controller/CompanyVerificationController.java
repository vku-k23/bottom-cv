package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.CompanyVerificationRequest;
import com.cnpm.bottomcv.dto.response.CompanyResponse;
import com.cnpm.bottomcv.dto.response.CompanyVerificationQueueResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.impl.CompanyVerificationServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Company Verification API", description = "API for company verification management")
@RestController
@RequestMapping(value = "/api/v1/back/companies", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class CompanyVerificationController {

    private final CompanyVerificationServiceImpl companyVerificationService;

    @GetMapping("/verification-queue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListResponse<CompanyVerificationQueueResponse>> getVerificationQueue(
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        ListResponse<CompanyVerificationQueueResponse> queue = 
            companyVerificationService.getVerificationQueue(verified, pageNo, pageSize);
        return ResponseEntity.ok(queue);
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyResponse> verifyCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyVerificationRequest request) {
        CompanyResponse response = companyVerificationService.verifyCompany(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject-verification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyResponse> rejectVerification(
            @PathVariable Long id,
            @Valid @RequestBody CompanyVerificationRequest request) {
        CompanyResponse response = companyVerificationService.rejectVerification(id, request);
        return ResponseEntity.ok(response);
    }
}

