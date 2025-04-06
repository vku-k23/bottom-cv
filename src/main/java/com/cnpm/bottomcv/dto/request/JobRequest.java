package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.JobType;
import com.cnpm.bottomcv.constant.StatusJob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class JobRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String jobDescription;

    @NotBlank(message = "Job requirement is required")
    private String jobRequirement;

    @NotBlank(message = "Job benefit is required")
    private String jobBenefit;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotBlank(message = "Location is required")
    private String location;

    private String workTime;

    private Double salary;

    private LocalDateTime expiryDate;

    private StatusJob status;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private Set<Long> categoryIds;
}