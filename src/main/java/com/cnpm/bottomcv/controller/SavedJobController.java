package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.SavedJobRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.SavedJobResponse;
import com.cnpm.bottomcv.service.SavedJobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Saved Jobs API", description = "Skeleton endpoints for managing saved jobs")
@RestController
@RequestMapping(value = "/api/v1/front/saved-jobs", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService savedJobService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<SavedJobResponse> saveJob(@Valid @RequestBody SavedJobRequest request) {
        var response = savedJobService.saveJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ListResponse<SavedJobResponse>> listSavedJobs(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortType) {
        var response = savedJobService.listSavedJobs(pageNo, pageSize, sortBy, sortType);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> removeSavedJob(@PathVariable Long jobId) {
        savedJobService.removeSavedJob(jobId);
        return ResponseEntity.noContent().build();
    }
}