package com.cnpm.bottomcv.dto.response;


import com.cnpm.bottomcv.constant.JobType;
import com.cnpm.bottomcv.constant.StatusJob;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime expiryDate;

    private StatusJob status;

    private List<CategoryResponse> categories;
}
