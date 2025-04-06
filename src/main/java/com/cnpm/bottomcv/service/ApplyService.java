package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.ApplyRequest;
import com.cnpm.bottomcv.dto.response.ApplyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;

public interface ApplyService {
    ApplyResponse createApply(ApplyRequest request);

    ApplyResponse getApplyById(Long id);

    ListResponse<ApplyResponse> getAllApplies(int pageNo, int pageSize, String sortBy, String sortType);

    ApplyResponse updateApply(Long id, ApplyRequest request);

    void deleteApply(Long id);
}
