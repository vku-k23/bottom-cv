package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.AlertFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobAlertRequest {
    @NotBlank
    private String keywords;

    private String location;

    @NotNull
    private AlertFrequency frequency;

    private boolean enabled = true;
}