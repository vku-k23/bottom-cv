package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.StatusJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationQueueResponse {
    private Long id;
    private String title;
    private String companyName;
    private Long companyId;
    private String location;
    private Double salary;
    private StatusJob status;
    private String jobType;
    private String submittedAt;
    private String submittedBy;
    private Long submittedById;
    private Integer reportCount; // number of reports on this job
}

