package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.ApplyRequest;
import com.cnpm.bottomcv.dto.request.UpdateApplicationStatusRequest;
import com.cnpm.bottomcv.dto.response.ApplyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

public interface ApplyService {
    ApplyResponse createApply(ApplyRequest request, Authentication authentication);

    ApplyResponse getApplyById(Long id, Authentication authentication);

    ListResponse<ApplyResponse> getAllApplies(int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication);

    ApplyResponse updateApply(Long id, ApplyRequest request, Authentication authentication);

    ApplyResponse submitApplication(Long jobId, String coverLetter, org.springframework.web.multipart.MultipartFile cvFile, Authentication authentication);

    void deleteApply(Long id, Authentication authentication);
    
    // New methods for Kanban board functionality
    ListResponse<ApplyResponse> getAppliesByJobId(Long jobId, int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication);
    
    ListResponse<ApplyResponse> getAppliesByJobIdAndStatus(Long jobId, com.cnpm.bottomcv.constant.StatusJob status, int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication);
    
    Map<com.cnpm.bottomcv.constant.StatusJob, List<ApplyResponse>> getAppliesGroupedByStatus(Long jobId, Authentication authentication);
    
    ApplyResponse updateApplicationStatus(Long id, UpdateApplicationStatusRequest request, Authentication authentication);
}
