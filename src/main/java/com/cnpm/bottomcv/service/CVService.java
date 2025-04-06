package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.CVRequest;
import com.cnpm.bottomcv.dto.response.CVResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;

public interface CVService {
    CVResponse createCV(CVRequest request);

    CVResponse updateCV( Long id, CVRequest request);

    void deleteCV(Long id);

    ListResponse<CVResponse> getAllCVs(int pageNo, int pageSize, String sortBy, String sortType);

    CVResponse getCVById(Long id);

    ListResponse<CVResponse> getAllCVsByUserId(Long userId, int pageNo, int pageSize, String sortBy, String sortType);
}
