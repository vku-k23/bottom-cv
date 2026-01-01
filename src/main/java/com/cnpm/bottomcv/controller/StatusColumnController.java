package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.CreateStatusColumnRequest;
import com.cnpm.bottomcv.dto.request.UpdateStatusColumnRequest;
import com.cnpm.bottomcv.dto.response.StatusColumnResponse;
import com.cnpm.bottomcv.service.StatusColumnService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Status Column API", description = "The API for managing status columns")
@RestController
@RequestMapping(value = "/api/v1/status-columns", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class StatusColumnController {
    private final StatusColumnService statusColumnService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<List<StatusColumnResponse>> getAllStatusColumns(
            @RequestParam(required = false) Long jobId,
            Authentication authentication) {
        List<StatusColumnResponse> columns = statusColumnService.getAllStatusColumns(jobId, authentication);
        return ResponseEntity.ok(columns);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<StatusColumnResponse> getStatusColumnById(
            @PathVariable Long id,
            Authentication authentication) {
        StatusColumnResponse column = statusColumnService.getStatusColumnById(id, authentication);
        return ResponseEntity.ok(column);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<StatusColumnResponse> createStatusColumn(
            @Valid @RequestBody CreateStatusColumnRequest request,
            Authentication authentication) {
        StatusColumnResponse column = statusColumnService.createStatusColumn(request, authentication);
        return new ResponseEntity<>(column, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<StatusColumnResponse> updateStatusColumn(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusColumnRequest request,
            Authentication authentication) {
        StatusColumnResponse column = statusColumnService.updateStatusColumn(id, request, authentication);
        return ResponseEntity.ok(column);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Void> deleteStatusColumn(
            @PathVariable Long id,
            Authentication authentication) {
        statusColumnService.deleteStatusColumn(id, authentication);
        return ResponseEntity.noContent().build();
    }
}

