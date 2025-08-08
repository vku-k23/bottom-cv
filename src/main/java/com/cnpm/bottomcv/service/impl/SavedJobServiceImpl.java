package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.SavedJobRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.SavedJobResponse;
import com.cnpm.bottomcv.service.SavedJobService;
import org.springframework.stereotype.Service;

@Service
public class SavedJobServiceImpl implements SavedJobService {
    @Override
    public SavedJobResponse saveJob(SavedJobRequest request) {
        return SavedJobResponse.builder().build();
    }

    @Override
    public ListResponse<SavedJobResponse> listSavedJobs(int pageNo, int pageSize, String sortBy, String sortType) {
        return ListResponse.<SavedJobResponse>builder().build();
    }

    @Override
    public void removeSavedJob(Long jobId) {
        // TODO: implement remove saved job
    }
}