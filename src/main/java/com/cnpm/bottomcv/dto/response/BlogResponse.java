package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.BlogStatus;
import com.cnpm.bottomcv.constant.TimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogResponse {

    private Long id;
    private String title;
    private String slug;
    private String thumbnail;
    private String content;
    private String excerpt;
    private BlogStatus status;

    // SEO Meta fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormat.DATE_TIME_FORMAT)
    private LocalDateTime publishedAt;

    private Long viewCount;

    // Author info
    private AuthorInfo author;

    // Category info
    private CategoryInfo category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormat.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TimeFormat.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String username;
        private String fullName;
        private String avatar;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CategoryInfo {
        private Long id;
        private String name;
    }
}

