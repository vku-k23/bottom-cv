package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.JobType;
import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.validation.InvalidWords.InvalidWords;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JobRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than or equal to 100 characters")
    @InvalidWords(message = "Title contains invalid words")
    private String title;

    @NotBlank(message = "Job description is required")
    @Size(max = 500, message = "Job description must be less than or equal to 500 characters")
    @InvalidWords(message = "Job description contains invalid words")
    private String jobDescription;

    @NotBlank(message = "Job requirement is required")
    @Size(max = 500, message = "Job requirement must be less than or equal to 500 characters")
    @InvalidWords(message = "Job requirement contains invalid words")
    private String jobRequirement;

    @NotBlank(message = "Job benefit is required")
    @Size(max = 500, message = "Job benefit must be less than or equal to 500 characters")
    @InvalidWords(message = "Job benefit contains invalid words")
    private String jobBenefit;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotBlank(message = "Location is required")
    private String location;

    // Optional: Geographic coordinates (can be auto-filled from location)
    private Double latitude;

    private Double longitude;

    @NotBlank(message = "Work time is required")
    @Size(max = 50, message = "Work time must be less than or equal to 50 characters")
    private String workTime;

    private Double salary;

    private LocalDateTime expiryDate;

    private StatusJob status;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private Set<Long> categoryIds;
}