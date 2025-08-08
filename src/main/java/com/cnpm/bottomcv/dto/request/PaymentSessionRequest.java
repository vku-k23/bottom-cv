package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentSessionRequest {
    @NotBlank
    private String provider; // STRIPE, VNPAY, ...

    @NotNull
    @Min(1)
    private Long amountMinor;

    private String currency = "VND";
}