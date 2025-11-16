package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyVerificationRequest {
    private Boolean verified;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes; // admin notes
    
    @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
    private String rejectionReason;
}

