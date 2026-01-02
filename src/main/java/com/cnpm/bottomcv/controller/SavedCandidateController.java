package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.constant.AppConstant;

import com.cnpm.bottomcv.dto.request.SavedCandidateRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.SavedCandidateResponse;
import com.cnpm.bottomcv.service.SavedCandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/admin/saved-candidates", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
@Tag(name = "Saved Candidates", description = "APIs for managing saved candidates by employers")
public class SavedCandidateController {

    private final SavedCandidateService savedCandidateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Save a candidate", description = "Save a candidate for later reference")
    public ResponseEntity<SavedCandidateResponse> saveCandidate(
            @Valid @RequestBody SavedCandidateRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedCandidateService.saveCandidate(request, authentication));
    }

    @PostMapping("/toggle")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Toggle save/unsave a candidate", description = "If saved, unsave. If not saved, save. Returns saved candidate or null if unsaved.")
    public ResponseEntity<Map<String, Object>> toggleSaveCandidate(
            @Valid @RequestBody SavedCandidateRequest request,
            Authentication authentication) {
        SavedCandidateResponse response = savedCandidateService.toggleSaveCandidate(request, authentication);
        boolean isSaved = response != null;
        return ResponseEntity.ok(Map.of(
                "saved", isSaved,
                AppConstant.RESPONSE_KEY_MESSAGE, isSaved ? "Candidate saved successfully" : "Candidate removed from saved list",
                "data", response != null ? response : Map.of()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Remove a saved candidate by ID")
    public ResponseEntity<Map<String, String>> removeSavedCandidate(
            @PathVariable Long id,
            Authentication authentication) {
        savedCandidateService.removeSavedCandidate(id, authentication);
        return ResponseEntity.ok(Map.of(AppConstant.RESPONSE_KEY_MESSAGE, "Saved candidate removed successfully"));
    }

    @DeleteMapping("/candidate/{candidateId}/job/{jobId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Remove a saved candidate by candidate ID and job ID")
    public ResponseEntity<Map<String, String>> removeSavedCandidateByIds(
            @PathVariable Long candidateId,
            @PathVariable Long jobId,
            Authentication authentication) {
        savedCandidateService.removeSavedCandidateByIds(candidateId, jobId, authentication);
        return ResponseEntity.ok(Map.of(AppConstant.RESPONSE_KEY_MESSAGE, "Saved candidate removed successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get all saved candidates", description = "Get all saved candidates for the current employer")
    public ResponseEntity<ListResponse<SavedCandidateResponse>> getSavedCandidates(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortType,
            Authentication authentication) {
        return ResponseEntity.ok(savedCandidateService.getSavedCandidates(
                pageNo, pageSize, sortBy, sortType, authentication));
    }

    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Check if a candidate is saved", description = "Check if a candidate is saved by the current employer for a specific job")
    public ResponseEntity<Map<String, Boolean>> isCandidateSaved(
            @RequestParam Long candidateId,
            @RequestParam Long jobId,
            Authentication authentication) {
        boolean isSaved = savedCandidateService.isCandidateSaved(candidateId, jobId, authentication);
        return ResponseEntity.ok(Map.of("saved", isSaved));
    }
}
