package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HireCandidateRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    private String note;

    // Offer details (optional)
    private BigDecimal salary;

    private String salaryCurrency;

    private LocalDate startDate;

    private String position;

    private String department;

    private String contractType; // FULL_TIME, PART_TIME, CONTRACT, etc.

    private String additionalBenefits;

    private Boolean sendOfferEmail; // Whether to send offer email automatically
}
