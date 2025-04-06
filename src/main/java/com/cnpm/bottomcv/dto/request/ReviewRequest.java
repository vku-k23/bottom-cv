package com.cnpm.bottomcv.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    @NotBlank(message = "Comment is required")
    private String comment;

    @NotNull(message = "Rating is required")
    private Integer rating;

    private Long companyId;
    private Long userId;
    private Long jobId;
}