package com.cnpm.bottomcv.dto.response;


import com.cnpm.bottomcv.constant.JobType;
import com.cnpm.bottomcv.constant.StatusJob;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobResponse {

    private Long id;

    private String title;

    private String jobDescription;

    private String jobRequirement;

    private String jobBenefit;

    private JobType jobType;

    private String location;

    private String workTime;

    private Double salary;

    private LocalDateTime expiryDate;

    private StatusJob status;

    private List<CategoryResponse> categories;
}
