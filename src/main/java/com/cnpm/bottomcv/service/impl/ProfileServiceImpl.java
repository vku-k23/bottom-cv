package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.dto.request.ProfileRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ProfileResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.exception.UnauthorizedException;
import com.cnpm.bottomcv.model.Profile;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.ApplyRepository;
import com.cnpm.bottomcv.repository.ProfileRepository;
import com.cnpm.bottomcv.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
  private final ProfileRepository profileRepository;
  private final ApplyRepository applyRepository;
  private final com.cnpm.bottomcv.service.MinioService minioService;

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
  public ProfileResponse getProfileByUserIdForEmployer(Long userId, Authentication authentication) {
    User currentUser = (User) authentication.getPrincipal();
    
    // Check if user has ADMIN or EMPLOYER role
    boolean hasAdminRole = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName() == RoleType.ADMIN);
    boolean hasEmployerRole = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName() == RoleType.EMPLOYER);
    
    if (!hasAdminRole && !hasEmployerRole) {
        throw new UnauthorizedException("Only ADMIN and EMPLOYER can view candidate profiles.");
    }
    
    // For EMPLOYER (without ADMIN role), verify the candidate has applied to their company's jobs
    if (hasEmployerRole && !hasAdminRole) {
        if (currentUser.getCompany() == null) {
            throw new ResourceNotFoundException("Company", "user", currentUser.getId().toString());
        }
        
        Long employerCompanyId = currentUser.getCompany().getId();
        
        // Check if the candidate has applied to any job of this employer's company
        boolean hasApplication = applyRepository.findByJob_CompanyId(employerCompanyId).stream()
                .anyMatch(apply -> apply.getUser().getId().equals(userId));
        
        if (!hasApplication) {
            throw new UnauthorizedException("You can only view profiles of candidates who applied to your company's jobs.");
        }
    }
    
    // ADMIN can view any profile, no additional checks needed
    return getProfileByUserId(userId);
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
    String avatarUrl = profile.getAvatar();
    if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.startsWith("http")) {
        try {
            avatarUrl = minioService.getFileUrl(avatarUrl);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for avatar: {}", avatarUrl);
        }
    }

    return ProfileResponse.builder()
        .id(profile.getId())
        .avatar(avatarUrl)
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
