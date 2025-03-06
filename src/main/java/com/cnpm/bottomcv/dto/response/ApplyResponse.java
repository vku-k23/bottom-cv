package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.model.CV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyResponse {
    private Long id;

    private UserResponse user;

    private JobResponse job;

    private CV cv;

    private StatusJob status;

    private String message;
}
