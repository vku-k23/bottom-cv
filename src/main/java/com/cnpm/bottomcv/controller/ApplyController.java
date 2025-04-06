package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.ApplyRequest;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Apply job API", description = "The API of apply job")
@RestController
@RequestMapping(value = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;

    @GetMapping("/back/applies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApplyResponse> getApplyByIdForBack(@PathVariable Long id) {
        ApplyResponse response = applyService.getApplyById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/back/applies")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ListResponse<ApplyResponse>> getAllAppliesForBack(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType
    ) {
        return ResponseEntity.ok(applyService.getAllApplies(pageNo, pageSize, sortBy, sortType));
    }

    @PutMapping("/back/applies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApplyResponse> updateApply(@PathVariable Long id, @Valid @RequestBody ApplyRequest request) {
        ApplyResponse response = applyService.updateApply(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/back/applies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Void> deleteApply(@PathVariable Long id) {
        applyService.deleteApply(id);
        return ResponseEntity.noContent().build();
    }

    // Front APIs (for client web - CANDIDATE)
    @PostMapping("/front/applies")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplyResponse> createApply(@Valid @RequestBody ApplyRequest request) {
        ApplyResponse response = applyService.createApply(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/front/applies/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplyResponse> getApplyByIdForFront(@PathVariable Long id) {
        ApplyResponse response = applyService.getApplyById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/front/applies")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ListResponse<ApplyResponse>> getAllAppliesForFront(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType
    ) {
        ListResponse<ApplyResponse> responses = applyService.getAllApplies(pageNo, pageSize, sortBy, sortType);
        return ResponseEntity.ok(responses);
    }
}
