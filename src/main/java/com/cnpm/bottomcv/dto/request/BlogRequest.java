package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.BlogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 255, message = "Slug must be less than 255 characters")
    private String slug; // Optional, will be auto-generated from title if not provided

    private String thumbnail;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500, message = "Excerpt must be less than 500 characters")
    private String excerpt;

    private BlogStatus status;

    // SEO Meta fields
    @Size(max = 255, message = "Meta title must be less than 255 characters")
    private String metaTitle;

    @Size(max = 500, message = "Meta description must be less than 500 characters")
    private String metaDescription;

    @Size(max = 255, message = "Meta keywords must be less than 255 characters")
    private String metaKeywords;

    private LocalDateTime publishedAt;

    private Long categoryId;
}

