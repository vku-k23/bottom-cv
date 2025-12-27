package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.ProfileRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ProfileResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Profile;
import com.cnpm.bottomcv.repository.ProfileRepository;
import com.cnpm.bottomcv.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
  private final ProfileRepository profileRepository;

  @Override
  public ListResponse<ProfileResponse> allProfiles(int pageNo, int pageSize, String sortBy, String sortType) {
    Sort sortObj = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
    Page<Profile> pageProfiles = profileRepository.findAll(pageable);
    List<Profile> profiles = pageProfiles.getContent();

    return ListResponse.<ProfileResponse>builder()
        .data(mapToProfileResponseList(profiles))
        .pageNo(pageProfiles.getNumber())
        .pageSize(pageProfiles.getSize())
        .totalElements((int) pageProfiles.getTotalElements())
        .totalPages(pageProfiles.getTotalPages())
        .isLast(pageProfiles.isLast())
        .build();
  }

  @Override
  public ProfileResponse getProfileByUserId(Long id) {
    return profileRepository.findByUserId(id)
        .map(this::mapToProfileResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", id.toString()));
  }

  @Override
  public ProfileResponse getProfileById(Long id) {
    return profileRepository.findById(id)
        .map(this::mapToProfileResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Profile id", "id", id.toString()));
  }

  @Override
  public ProfileResponse createProfile(ProfileRequest profileRequest) {
    Profile profile = Profile.builder()
        .firstName(profileRequest.getFirstName())
        .lastName(profileRequest.getLastName())
        .dayOfBirth(profileRequest.getDayOfBirth())
        .address(profileRequest.getAddress())
        .phoneNumber(profileRequest.getPhoneNumber())
        .email(profileRequest.getEmail())
        .avatar(profileRequest.getAvatar())
        .description(profileRequest.getDescription())
        .build();
    return mapToProfileResponse(profileRepository.save(profile));
  }

  @Override
  public ProfileResponse updateProfile(Long id, ProfileRequest profileRequest) {
    Profile profile = profileRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Profile id", "id", id.toString()));

    profile.setFirstName(profileRequest.getFirstName().trim());
    profile.setLastName(profileRequest.getLastName().trim());
    profile.setDayOfBirth(profileRequest.getDayOfBirth());
    profile.setAddress(profileRequest.getAddress() == null ? "" : profileRequest.getAddress().trim());
    profile.setPhoneNumber(profileRequest.getPhoneNumber().trim());
    profile.setEmail(profileRequest.getEmail().trim());
    profile.setAvatar(profileRequest.getAvatar() == null ? "" : profileRequest.getAvatar().trim());
    profile.setDescription(
        profileRequest.getDescription() == null ? "" : profileRequest.getDescription().trim());

    return mapToProfileResponse(profileRepository.save(profile));
  }

  @Override
  public void deleteProfile(Long id) {
    Profile profile = profileRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Profile id", "id", id.toString()));
    profileRepository.delete(profile);
  }

  private List<ProfileResponse> mapToProfileResponseList(List<Profile> profiles) {
    return profiles.stream()
        .map(this::mapToProfileResponse)
        .toList();
  }

  private ProfileResponse mapToProfileResponse(Profile profile) {
    return ProfileResponse.builder()
        .id(profile.getId())
        .avatar(profile.getAvatar())
        .firstName(profile.getFirstName())
        .lastName(profile.getLastName())
        .dayOfBirth(profile.getDayOfBirth())
        .description(profile.getDescription())
        .email(profile.getEmail())
        .phoneNumber(profile.getPhoneNumber())
        .address(profile.getAddress())
        .createdAt(profile.getCreatedAt().toString())
        .updatedAt(profile.getUpdatedAt().toString())
        .build();
  }
}
