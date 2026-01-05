package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.SubscriptionPlanType;
import com.cnpm.bottomcv.constant.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private Long companyId;
    private SubscriptionPlanType planType;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
}

