package com.cnpm.bottomcv.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private String provider;
    private String status;
    private String referenceId;
    private String currency;
    private Long amountMinor;
}