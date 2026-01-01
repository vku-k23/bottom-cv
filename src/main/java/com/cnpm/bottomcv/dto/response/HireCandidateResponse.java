package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HireCandidateResponse {
    
    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long jobId;
    private String jobTitle;
    private ApplicationStatus previousStatus;
    private ApplicationStatus newStatus;
    private LocalDateTime hiredAt;
    private String hiredBy;
    
    // Offer details
    private BigDecimal salary;
    private String salaryCurrency;
    private LocalDate startDate;
    private String position;
    private String department;
    private String contractType;
    
    private String message;
    private Boolean emailSent;
}

