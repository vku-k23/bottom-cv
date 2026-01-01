package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.SavedCandidateRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.SavedCandidateResponse;
import org.springframework.security.core.Authentication;

public interface SavedCandidateService {

    /**
     * Save a candidate for the current employer
     */
    SavedCandidateResponse saveCandidate(SavedCandidateRequest request, Authentication authentication);

    /**
     * Remove a saved candidate
     */
    void removeSavedCandidate(Long id, Authentication authentication);

    /**
     * Remove a saved candidate by candidate ID and job ID
     */
    void removeSavedCandidateByIds(Long candidateId, Long jobId, Authentication authentication);

    /**
     * Get all saved candidates for the current employer with pagination
     */
    ListResponse<SavedCandidateResponse> getSavedCandidates(
            int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication);

    /**
     * Check if a candidate is saved by the current employer for a specific job
     */
    boolean isCandidateSaved(Long candidateId, Long jobId, Authentication authentication);

    /**
     * Toggle save/unsave a candidate (returns saved status after toggle)
     */
    SavedCandidateResponse toggleSaveCandidate(SavedCandidateRequest request, Authentication authentication);
}

