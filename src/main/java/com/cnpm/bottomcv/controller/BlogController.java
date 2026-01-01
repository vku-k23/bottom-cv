package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.BlogRequest;
import com.cnpm.bottomcv.dto.request.BlogSearchRequest;
import com.cnpm.bottomcv.dto.response.BlogResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.BlogService;
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

import java.util.List;

@Tag(name = "Blog API", description = "Blog management endpoints")
@RestController
@RequestMapping(value = "/api/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    // ==================== Admin APIs (ADMIN only) ====================

    @Operation(summary = "Create a new blog post (Admin only)")
    @PostMapping("/back/blogs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlogResponse> createBlog(
            @Valid @RequestBody BlogRequest request,
            Authentication authentication) {
        BlogResponse response = blogService.createBlog(request, authentication);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a blog post (Admin only)")
    @PutMapping("/back/blogs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlogResponse> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogRequest request,
            Authentication authentication) {
        BlogResponse response = blogService.updateBlog(id, request, authentication);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a blog post (Admin only)")
    @DeleteMapping("/back/blogs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a blog by ID (Admin only)")
    @GetMapping("/back/blogs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        BlogResponse response = blogService.getBlogById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all blogs with filters (Admin only)")
    @GetMapping("/back/blogs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListResponse<BlogResponse>> getAllBlogs(@ModelAttribute BlogSearchRequest request) {
        ListResponse<BlogResponse> response = blogService.getAllBlogs(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get blog statistics (Admin only)")
    @GetMapping("/back/blogs/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlogStatsResponse> getBlogStats() {
        BlogStatsResponse stats = BlogStatsResponse.builder()
                .publishedCount(blogService.countPublishedBlogs())
                .draftCount(blogService.countDraftBlogs())
                .build();
        return ResponseEntity.ok(stats);
    }

    // ==================== Public APIs (for all users including guests) ====================

    @Operation(summary = "Get published blog by slug (Public)")
    @GetMapping("/front/blogs/{slug}")
    public ResponseEntity<BlogResponse> getPublishedBlogBySlug(@PathVariable String slug) {
        BlogResponse response = blogService.getPublishedBlogBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all published blogs with pagination and filters (Public)")
    @GetMapping("/front/blogs")
    public ResponseEntity<ListResponse<BlogResponse>> getPublishedBlogs(@ModelAttribute BlogSearchRequest request) {
        ListResponse<BlogResponse> response = blogService.getPublishedBlogs(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get recent published blogs (Public)")
    @GetMapping("/front/blogs/recent")
    public ResponseEntity<List<BlogResponse>> getRecentBlogs() {
        List<BlogResponse> response = blogService.getRecentBlogs();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get related blogs (Public)")
    @GetMapping("/front/blogs/{id}/related")
    public ResponseEntity<List<BlogResponse>> getRelatedBlogs(@PathVariable Long id) {
        List<BlogResponse> response = blogService.getRelatedBlogs(id);
        return ResponseEntity.ok(response);
    }

    // ==================== Inner Classes ====================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BlogStatsResponse {
        private Long publishedCount;
        private Long draftCount;
    }
}

