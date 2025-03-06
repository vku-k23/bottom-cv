package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.JobType;
import com.cnpm.bottomcv.constant.StatusJob;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String jobDescription;

    @NotBlank(message = "Job requirement is required")
    private String jobRequirement;

    @NotBlank(message = "Job benefit is required")
    private String jobBenefit;

    private JobType jobType;

    private String location;

    private String workTime;

    private Double salary;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime expiryDate;

    private StatusJob status;

}
