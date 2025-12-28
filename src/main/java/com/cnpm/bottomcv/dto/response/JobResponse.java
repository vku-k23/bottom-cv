package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.JobType;
import com.cnpm.bottomcv.constant.StatusJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {

    private Long id;
    private String title;
    private String jobDescription;
    private String jobRequirement;
    private String jobBenefit;
    private JobType jobType;
    private String location;
    private Double latitude;
    private Double longitude;
    private String workTime;
    private Double salary;
    private String careerLevel;
    private String qualification;
    private String experience;
    private LocalDateTime expiryDate;
    private StatusJob status;
    private CompanyResponse company;
    private Set<CategoryResponse> categories;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}