package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.JobType;
import com.cnpm.bottomcv.constant.StatusJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobRequest {

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

}
