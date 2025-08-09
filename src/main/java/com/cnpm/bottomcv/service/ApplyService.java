package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.ApplyRequest;
import com.cnpm.bottomcv.dto.response.ApplyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import org.springframework.security.core.Authentication;

public interface ApplyService {
    ApplyResponse createApply(ApplyRequest request, Authentication authentication);

    ApplyResponse getApplyById(Long id, Authentication authentication);

    ListResponse<ApplyResponse> getAllApplies(int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication);

    ApplyResponse updateApply(Long id, ApplyRequest request, Authentication authentication);

    void deleteApply(Long id, Authentication authentication);
}
