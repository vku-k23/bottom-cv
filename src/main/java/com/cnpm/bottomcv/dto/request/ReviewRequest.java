package com.cnpm.bottomcv.dto.request;


import com.cnpm.bottomcv.validation.InvalidWords.InvalidWords;
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
    @InvalidWords(message = "Comment contains invalid words")
    private String comment;

    @NotNull(message = "Rating is required")
    private Integer rating;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "User ID is required")
    private Long userId;
}