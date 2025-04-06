package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.JobType;
import com.cnpm.bottomcv.constant.StatusJob;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class JobSearchRequest {
    private String keyword;
    private String location;
    private JobType jobType;
    @Min(value = 0, message = "Minimum salary must be non-negative")
    private Double minSalary;
    @Min(value = 0, message = "Maximum salary must be non-negative")
    private Double maxSalary;
    private Long categoryId;
    private StatusJob status;
    private String sortBy;
    private String sortDirection;
    @Min(value = 0, message = "Page number must be non-negative")
    private int page;
    @Min(value = 1, message = "Page size must be at least 1")
    private int size;
}