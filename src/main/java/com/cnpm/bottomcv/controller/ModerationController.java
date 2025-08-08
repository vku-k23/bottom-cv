package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.service.ModerationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Moderation API", description = "Skeleton endpoints for moderating jobs and reviews")
@RestController
@RequestMapping(value = "/api/v1/back/moderation", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    @PostMapping("/jobs/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveJob(@PathVariable Long id) {
        moderationService.approveJob(id);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/jobs/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectJob(@PathVariable Long id, @RequestBody(required = false) String reason) {
        moderationService.rejectJob(id, reason);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/reviews/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveReview(@PathVariable Long id) {
        moderationService.approveReview(id);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/reviews/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectReview(@PathVariable Long id, @RequestBody(required = false) String reason) {
        moderationService.rejectReview(id, reason);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}