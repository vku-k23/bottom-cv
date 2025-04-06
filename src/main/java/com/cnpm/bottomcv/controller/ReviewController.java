package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.ReviewRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ReviewResponse;
import com.cnpm.bottomcv.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review API", description = "The API of review")
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Back APIs (for dashboard - ADMIN)
    @GetMapping("/back/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> getReviewByIdForBack(@PathVariable Long id) {
        ReviewResponse response = reviewService.getReviewById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/back/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListResponse<ReviewResponse>> getAllReviewsForBack(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType
    ) {
        return ResponseEntity.ok(reviewService.getAllReviews(pageNo, pageSize, sortBy, sortType));
    }

    @DeleteMapping("/back/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    // Front APIs (for client web - CANDIDATE or public)
    @PostMapping("/front/reviews")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/front/reviews/{id}")
    public ResponseEntity<ReviewResponse> getReviewByIdForFront(@PathVariable Long id) {
        ReviewResponse response = reviewService.getReviewById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/front/reviews")
    public ResponseEntity<ListResponse<ReviewResponse>> getAllReviewsForFront(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType
    ) {
        return ResponseEntity.ok(reviewService.getAllReviews(pageNo, pageSize, sortBy, sortType));
    }

    @PutMapping("/front/reviews/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(id, request);
        return ResponseEntity.ok(response);
    }
}