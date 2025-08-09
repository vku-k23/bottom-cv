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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Apply job API", description = "The API of apply job")
@RestController
@RequestMapping(value = "/api/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;

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
}
