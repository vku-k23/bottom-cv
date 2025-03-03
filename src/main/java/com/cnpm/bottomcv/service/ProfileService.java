package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.ProfileRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ProfileResponse;

public interface ProfileService {
    ListResponse<ProfileResponse> allProfiles(int pageNo, int pageSize, String sortBy, String sortType);

    ProfileResponse getProfileById(Long id);

    ProfileResponse createProfile(ProfileRequest profileRequest);

    ProfileResponse updateProfile(Long id, ProfileRequest profileRequest);

    void deleteProfile(Long id);
}
