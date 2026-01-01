package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.HireCandidateRequest;
import com.cnpm.bottomcv.dto.response.ApplicationStatusHistoryResponse;
import com.cnpm.bottomcv.dto.response.HireCandidateResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface HireCandidateService {
    
    /**
     * Hire a candidate by updating their application status to HIRED
     */
    HireCandidateResponse hireCandidate(HireCandidateRequest request, Authentication authentication);
    
    /**
     * Get the status history for an application
     */
    List<ApplicationStatusHistoryResponse> getApplicationStatusHistory(Long applicationId, Authentication authentication);
    
    /**
     * Update application status with history tracking
     */
    ApplicationStatusHistoryResponse updateApplicationStatus(Long applicationId, String newStatus, String note, Authentication authentication);
}

