package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.ReportRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ReportResponse;
import com.cnpm.bottomcv.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reports API", description = "Skeleton endpoints for abuse reports")
@RestController
@RequestMapping(value = "/api/v1", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/front/reports")
    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody ReportRequest request) {
        var response = reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/back/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListResponse<ReportResponse>> listReports(
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        var response = reportService.listReports(resolved, pageNo, pageSize);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/back/reports/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> resolveReport(@PathVariable Long id) {
        var response = reportService.resolveReport(id);
        return ResponseEntity.ok(response);
    }
}