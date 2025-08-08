package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.AlertFrequency;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobAlertResponse {
    private Long id;
    private String keywords;
    private String location;
    private AlertFrequency frequency;
    private boolean enabled;
}