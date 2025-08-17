package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.ReviewRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ReviewResponse;
import com.cnpm.bottomcv.exception.BadRequestException;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Company;
import com.cnpm.bottomcv.model.Review;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.CompanyRepository;
import com.cnpm.bottomcv.repository.ReviewRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final CompanyRepository companyRepository;
  private final UserRepository userRepository;

  @Override
  public ReviewResponse createReview(ReviewRequest request) {
    if (reviewRepository.existsByUser_IdAndCompany_Id(request.getUserId(), request.getCompanyId())) {
      throw new BadRequestException("User has already reviewed this company");
    }
    Review review = new Review();
    mapRequestToEntity(review, request);
    review.setCreatedAt(LocalDateTime.now());
    review.setCreatedBy("system"); // Replace with actual user
    reviewRepository.save(review);
    return mapToResponse(review);
  }

  @Override
  public ReviewResponse getReviewById(Long id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Review id", "id", id.toString()));
    return mapToResponse(review);
  }

  @Override
  public ListResponse<ReviewResponse> getAllReviews(int pageNo, int pageSize, String sortBy, String sortType) {
    Sort sortObj = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
    Page<Review> pageReview = reviewRepository.findAll(pageable);
    List<Review> reviews = pageReview.getContent();

    return ListResponse.<ReviewResponse>builder()
        .data(mapToListResponse(reviews))
        .pageNo(pageReview.getNumber())
        .pageSize(pageReview.getSize())
        .totalElements((int) pageReview.getTotalElements())
        .totalPages(pageReview.getTotalPages())
        .isLast(pageReview.isLast())
        .build();
  }

  private List<ReviewResponse> mapToListResponse(List<Review> reviews) {
    return reviews.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public ReviewResponse updateReview(Long id, ReviewRequest request) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Review id", "id", id.toString()));

    mapRequestToEntity(review, request);
    review.setUpdatedAt(LocalDateTime.now());
    review.setUpdatedBy("system");
    reviewRepository.save(review);
    return mapToResponse(review);
  }

  @Override
  public void deleteReview(Long id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Review id", "id", id.toString()));
    reviewRepository.delete(review);
  }

  private void mapRequestToEntity(Review review, ReviewRequest request) {
    review.setComment(request.getComment());
    review.setRating(request.getRating());

    Company company = companyRepository.findById(request.getCompanyId())
        .orElseThrow(() -> new ResourceNotFoundException("Company id", "companyId",
            request.getCompanyId().toString()));
    review.setCompany(company);

    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User id", "userId", request.getUserId().toString()));
    review.setUser(user);
  }

  private ReviewResponse mapToResponse(Review review) {
    ReviewResponse response = new ReviewResponse();
    response.setId(review.getId());
    response.setComment(review.getComment());
    response.setRating(review.getRating());
    response.setCompanyId(review.getCompany().getId());
    response.setUserId(review.getUser().getId());
    response.setCreatedAt(review.getCreatedAt());
    response.setCreatedBy(review.getCreatedBy());
    response.setUpdatedAt(review.getUpdatedAt());
    response.setUpdatedBy(review.getUpdatedBy());
    return response;
  }
}
