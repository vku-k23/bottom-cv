package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.ReviewRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ReviewResponse;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);

    ReviewResponse getReviewById(Long id);

    ListResponse<ReviewResponse> getAllReviews(int pageNo, int pageSize, String sortBy, String sortType);

    ReviewResponse updateReview(Long id, ReviewRequest request);

    void deleteReview(Long id);
}
