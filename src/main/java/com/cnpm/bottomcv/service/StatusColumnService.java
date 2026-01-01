package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.CreateStatusColumnRequest;
import com.cnpm.bottomcv.dto.request.UpdateStatusColumnRequest;
import com.cnpm.bottomcv.dto.response.StatusColumnResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface StatusColumnService {
    List<StatusColumnResponse> getAllStatusColumns(Long jobId, Authentication authentication);

    StatusColumnResponse createStatusColumn(CreateStatusColumnRequest request, Authentication authentication);

    StatusColumnResponse updateStatusColumn(Long id, UpdateStatusColumnRequest request, Authentication authentication);

    void deleteStatusColumn(Long id, Authentication authentication);

    StatusColumnResponse getStatusColumnById(Long id, Authentication authentication);
}

