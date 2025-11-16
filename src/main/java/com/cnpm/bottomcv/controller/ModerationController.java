package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.request.BulkModerationRequest;
import com.cnpm.bottomcv.dto.request.ModerationRequest;
import com.cnpm.bottomcv.dto.response.JobResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ModerationQueueResponse;
import com.cnpm.bottomcv.service.ModerationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Moderation API", description = "API for moderating jobs and reviews")
@RestController
@RequestMapping(value = "/api/v1/back/moderation", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    @GetMapping("/jobs/queue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListResponse<ModerationQueueResponse>> getModerationQueue(
            @RequestParam(required = false) StatusJob status,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        ListResponse<ModerationQueueResponse> queue = moderationService.getModerationQueue(status, pageNo, pageSize);
        return ResponseEntity.ok(queue);
    }

    @PostMapping("/jobs/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobResponse> approveJob(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ModerationRequest request) {
        ModerationRequest req = request != null ? request : new ModerationRequest();
        JobResponse response = moderationService.approveJob(id, req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobResponse> rejectJob(
            @PathVariable Long id,
            @Valid @RequestBody ModerationRequest request) {
        JobResponse response = moderationService.rejectJob(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/bulk-approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> bulkApproveJobs(@Valid @RequestBody BulkModerationRequest request) {
        moderationService.bulkApproveJobs(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/jobs/bulk-reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> bulkRejectJobs(@Valid @RequestBody BulkModerationRequest request) {
        moderationService.bulkRejectJobs(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reviews/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveReview(@PathVariable Long id) {
        moderationService.approveReview(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reviews/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectReview(
            @PathVariable Long id,
            @RequestBody(required = false) String reason) {
        moderationService.rejectReview(id, reason);
        return ResponseEntity.ok().build();
    }
}