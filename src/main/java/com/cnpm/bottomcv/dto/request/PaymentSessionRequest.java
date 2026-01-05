package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.SubscriptionPlanType;
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

    private String currency = "USD";

    @NotNull
    private SubscriptionPlanType planType; // BASIC, STANDARD, PREMIUM

    @NotNull
    private Long companyId;
}