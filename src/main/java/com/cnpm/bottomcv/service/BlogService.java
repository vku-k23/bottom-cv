package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.BlogRequest;
import com.cnpm.bottomcv.dto.request.BlogSearchRequest;
import com.cnpm.bottomcv.dto.response.BlogResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface BlogService {

    // Admin APIs
    BlogResponse createBlog(BlogRequest request, Authentication authentication);

    BlogResponse updateBlog(Long id, BlogRequest request, Authentication authentication);

    void deleteBlog(Long id);

    BlogResponse getBlogById(Long id);

    ListResponse<BlogResponse> getAllBlogs(BlogSearchRequest request);

    // Public APIs
    BlogResponse getPublishedBlogBySlug(String slug);

    ListResponse<BlogResponse> getPublishedBlogs(BlogSearchRequest request);

    List<BlogResponse> getRecentBlogs();

    List<BlogResponse> getRelatedBlogs(Long blogId);

    // Stats
    Long countPublishedBlogs();

    Long countDraftBlogs();
}

