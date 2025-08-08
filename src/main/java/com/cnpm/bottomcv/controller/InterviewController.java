package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.InterviewRequest;
import com.cnpm.bottomcv.dto.response.InterviewResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.InterviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Interviews API", description = "Skeleton endpoints for interview scheduling and management")
@RestController
@RequestMapping(value = "/api/v1", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    // Back (employer/admin)
    @PostMapping("/back/interviews")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public ResponseEntity<InterviewResponse> createInterview(@Valid @RequestBody InterviewRequest request) {
        var response = interviewService.createInterview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/back/interviews")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public ResponseEntity<ListResponse<InterviewResponse>> listInterviews(@RequestParam(required = false) Long jobId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        var response = interviewService.listInterviews(jobId, pageNo, pageSize);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/back/interviews/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public ResponseEntity<InterviewResponse> updateInterview(@PathVariable Long id,
            @Valid @RequestBody InterviewRequest request) {
        var response = interviewService.updateInterview(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/back/interviews/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id) {
        interviewService.deleteInterview(id);
        return ResponseEntity.noContent().build();
    }

    // Front (candidate)
    @PostMapping("/front/interviews/{id}/confirm")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> confirmInterview(@PathVariable Long id) {
        interviewService.confirmInterview(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/front/interviews/{id}/decline")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> declineInterview(@PathVariable Long id) {
        interviewService.declineInterview(id);
        return ResponseEntity.ok().build();
    }
}