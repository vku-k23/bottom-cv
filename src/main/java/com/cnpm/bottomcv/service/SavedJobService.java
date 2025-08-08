package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.SavedJobRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.SavedJobResponse;

public interface SavedJobService {
    SavedJobResponse saveJob(SavedJobRequest request);

    ListResponse<SavedJobResponse> listSavedJobs(int pageNo, int pageSize, String sortBy, String sortType);

    void removeSavedJob(Long jobId);
}