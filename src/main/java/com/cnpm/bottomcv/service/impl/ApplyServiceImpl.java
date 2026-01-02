package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.AppConstant;
import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.request.ApplyRequest;
import com.cnpm.bottomcv.dto.request.UpdateApplicationStatusRequest;
import com.cnpm.bottomcv.dto.response.ApplyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.BadRequestException;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.exception.UnauthorizedException;
import com.cnpm.bottomcv.model.Apply;
import com.cnpm.bottomcv.model.CV;
import com.cnpm.bottomcv.model.Job;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.ApplyRepository;
import com.cnpm.bottomcv.repository.CVRepository;
import com.cnpm.bottomcv.repository.JobRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.ApplyService;
import com.cnpm.bottomcv.service.StatusColumnService;
import com.cnpm.bottomcv.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplyServiceImpl implements ApplyService {

    private final ApplyRepository applyRepository;
    private final CVRepository cvRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final com.cnpm.bottomcv.service.MinioService minioService;
    private final StatusColumnService statusColumnService;

    @Override
    public ApplyResponse createApply(ApplyRequest request, Authentication authentication) {

        RoleType currentRole = Helper.getCurrentRole(authentication);

        switch (currentRole) {
            case CANDIDATE:
                Apply apply = new Apply();
                mapRequestToEntity(apply, request, authentication);
                apply.setCreatedAt(LocalDateTime.now());
                apply.setCreatedBy(authentication.getName());
                applyRepository.save(apply);
                return mapToResponse(apply);
            case EMPLOYER, ADMIN:
                throw new BadRequestException("Only candidates can create an application.");
            default:
                throw new BadRequestException("Invalid role for creating an application.");
        }
    }

    @Override
    public ApplyResponse getApplyById(Long id, Authentication authentication) {
        RoleType currentRole = Helper.getCurrentRole(authentication);
        Apply apply = applyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_APPLY_ID_LABEL,
                        AppConstant.FIELD_APPLY_ID, id.toString()));

        switch (currentRole) {
            case CANDIDATE:
                User user = userRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("User", AppConstant.FIELD_USERNAME,
                                authentication.getName()));
                if (!apply.getUser().getId().equals(user.getId())) {
                    throw new UnauthorizedException("You can only view your own applications.");
                }
                return mapToResponse(apply);
            case EMPLOYER:
                User employer = (User) authentication.getPrincipal();
                if (employer.getCompany() == null) {
                    throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
                }
                Long employerCompanyId = employer.getCompany().getId();
                Long applicationCompanyId = apply.getJob().getCompany().getId();
                if (!employerCompanyId.equals(applicationCompanyId)) {
                    throw new UnauthorizedException("You can only view applications for your company's jobs.");
                }
                return mapToResponse(apply);
            case ADMIN:
                return mapToResponse(apply);
            default:
                throw new BadRequestException("Invalid role for viewing an application.");
        }
    }

    @Override
    public ListResponse<ApplyResponse> getAllApplies(int pageNo, int pageSize, String sortBy, String sortType,
            Authentication authentication) {

        RoleType currentRole = Helper.getCurrentRole(authentication);

        switch (currentRole) {
            case CANDIDATE:
                User user = userRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("User", AppConstant.FIELD_USERNAME,
                                authentication.getName()));
                Sort sortObj = sortType.equalsIgnoreCase("ASC")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
                Page<Apply> pageApply = applyRepository.findByUserId(user.getId(), pageable);
                List<Apply> applies = pageApply.getContent();

                return ListResponse.<ApplyResponse>builder()
                        .data(mapToApplyList(applies))
                        .pageNo(pageApply.getNumber())
                        .pageSize(pageApply.getSize())
                        .totalElements((int) pageApply.getTotalElements())
                        .totalPages(pageApply.getTotalPages())
                        .isLast(pageApply.isLast())
                        .build();
            case EMPLOYER:
                User employer = (User) authentication.getPrincipal();
                if (employer.getCompany() == null) {
                    throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
                }
                Long companyId = employer.getCompany().getId();
                Sort employerSortObj = sortType.equalsIgnoreCase("ASC")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();
                Pageable employerPageable = PageRequest.of(pageNo, pageSize, employerSortObj);
                Page<Apply> employerPageApply = applyRepository.findByJob_CompanyId(companyId, employerPageable);
                List<Apply> employerApplies = employerPageApply.getContent();

                return ListResponse.<ApplyResponse>builder()
                        .data(mapToApplyList(employerApplies))
                        .pageNo(employerPageApply.getNumber())
                        .pageSize(employerPageApply.getSize())
                        .totalElements((int) employerPageApply.getTotalElements())
                        .totalPages(employerPageApply.getTotalPages())
                        .isLast(employerPageApply.isLast())
                        .build();
            case ADMIN:
                Sort adminSortObj = sortType.equalsIgnoreCase("ASC")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();
                Pageable adminPageable = PageRequest.of(pageNo, pageSize, adminSortObj);
                Page<Apply> adminPageApply = applyRepository.findAll(adminPageable);
                List<Apply> adminApplies = adminPageApply.getContent();

                return ListResponse.<ApplyResponse>builder()
                        .data(mapToApplyList(adminApplies))
                        .pageNo(adminPageApply.getNumber())
                        .pageSize(adminPageApply.getSize())
                        .totalElements((int) adminPageApply.getTotalElements())
                        .totalPages(adminPageApply.getTotalPages())
                        .isLast(adminPageApply.isLast())
                        .build();
            default:
                throw new BadRequestException("You do not have permission to view applications.");
        }
    }

    @Override
    public ApplyResponse updateApply(Long id, ApplyRequest request, Authentication authentication) {

        RoleType currentRole = Helper.getCurrentRole(authentication);

        switch (currentRole) {
            case CANDIDATE:
                User user = userRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("User", AppConstant.FIELD_USERNAME,
                                authentication.getName()));
                if (!applyRepository.existsByIdAndUserId(id, user.getId())) {
                    throw new ResourceNotFoundException(AppConstant.FIELD_APPLY_ID_LABEL, AppConstant.FIELD_APPLY_ID,
                            id.toString());
                }
                Apply apply = applyRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_APPLY_ID_LABEL,
                                AppConstant.FIELD_APPLY_ID, id.toString()));

                mapRequestToEntity(apply, request, authentication);
                apply.setUpdatedAt(LocalDateTime.now());
                apply.setUpdatedBy(authentication.getName());
                applyRepository.save(apply);
                return mapToResponse(apply);
            case EMPLOYER:
                User employer = (User) authentication.getPrincipal();
                if (employer.getCompany() == null) {
                    throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
                }
                Apply employerApply = applyRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_APPLY_ID_LABEL,
                                AppConstant.FIELD_APPLY_ID, id.toString()));

                Long employerCompanyId = employer.getCompany().getId();
                Long applicationCompanyId = employerApply.getJob().getCompany().getId();
                if (!employerCompanyId.equals(applicationCompanyId)) {
                    throw new UnauthorizedException("You can only update applications for your company's jobs.");
                }

                // For employers, only update status and message (not userId, jobId, cvId)
                employerApply.setStatus(request.getStatus());
                employerApply.setMessage(request.getMessage());
                employerApply.setUpdatedAt(LocalDateTime.now());
                employerApply.setUpdatedBy(authentication.getName());
                applyRepository.save(employerApply);
                return mapToResponse(employerApply);
            case ADMIN:
                Apply adminApply = applyRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_APPLY_ID_LABEL,
                                AppConstant.FIELD_APPLY_ID, id.toString()));
                mapRequestToEntity(adminApply, request, authentication);
                adminApply.setUpdatedAt(LocalDateTime.now());
                adminApply.setUpdatedBy(authentication.getName());
                applyRepository.save(adminApply);
                return mapToResponse(adminApply);
            default:
                throw new BadRequestException("Invalid role for updating an application.");
        }
    }

    @Override
    @Transactional
    public ApplyResponse submitApplication(Long jobId, String coverLetter,
            org.springframework.web.multipart.MultipartFile cvFile, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", AppConstant.FIELD_USERNAME, username));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_JOB_ID, "jobId", jobId.toString()));

        Apply apply = new Apply();
        apply.setJob(job);
        apply.setUser(user);
        apply.setCoverLetter(coverLetter);
        apply.setMessage(coverLetter); // Keep backwards compatibility
        apply.setStatus(com.cnpm.bottomcv.constant.StatusJob.PENDING);
        apply.setCreatedAt(LocalDateTime.now());
        apply.setCreatedBy(username);

        if (cvFile != null && !cvFile.isEmpty()) {
            String cvUrl = minioService.uploadFile(cvFile, "applications/" + user.getId());
            apply.setCvUrl(cvUrl);
        }

        applyRepository.save(apply);
        return mapToResponse(apply);
    }

    @Override
    public void deleteApply(Long id, Authentication authentication) {
        RoleType currentRole = Helper.getCurrentRole(authentication);
        switch (currentRole) {
            case CANDIDATE:
                User user = userRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("User", AppConstant.FIELD_USERNAME,
                                authentication.getName()));
                if (!applyRepository.existsByIdAndUserId(id, user.getId())) {
                    throw new ResourceNotFoundException(AppConstant.FIELD_APPLY_ID_LABEL, AppConstant.FIELD_APPLY_ID,
                            id.toString());
                }
                applyRepository.deleteById(id);
                break;
            case EMPLOYER:
                User employer = (User) authentication.getPrincipal();
                if (employer.getCompany() == null) {
                    throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
                }
                Apply employerApply = applyRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_APPLY_ID_LABEL,
                                AppConstant.FIELD_APPLY_ID, id.toString()));

                Long employerCompanyId = employer.getCompany().getId();
                Long applicationCompanyId = employerApply.getJob().getCompany().getId();
                if (!employerCompanyId.equals(applicationCompanyId)) {
                    throw new UnauthorizedException("You can only delete applications for your company's jobs.");
                }
                applyRepository.deleteById(id);
                break;
            case ADMIN:
                applyRepository.deleteById(id);
                break;
            default:
                throw new BadRequestException("Invalid role for deleting an application.");
        }
    }

    private void mapRequestToEntity(Apply apply, ApplyRequest request, Authentication authentication) {
        apply.setMessage(request.getMessage());
        apply.setStatus(request.getStatus());

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", AppConstant.FIELD_USERNAME, username));
        Long userId = user.getId();

        CV cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_CV_ID, "cvId",
                        request.getCvId().toString()));
        if (!cv.getUser().getId().equals(userId)) {
            throw new BadRequestException(
                    String.format("CV id %s and user id %s do not match!", request.getCvId(), userId));
        }
        apply.setCv(cv);

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job id", "jobId", request.getJobId().toString()));
        apply.setJob(job);
        apply.setUser(user);
    }

    private List<ApplyResponse> mapToApplyList(List<Apply> applies) {
        return applies.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ApplyResponse mapToResponse(Apply apply) {
        ApplyResponse response = new ApplyResponse();
        response.setId(apply.getId());
        response.setMessage(apply.getMessage());
        response.setStatus(apply.getStatus());
        response.setPosition(apply.getPosition());
        if (apply.getCv() != null) {
            response.setCvId(apply.getCv().getId());
        }
        response.setCvUrl(apply.getCvUrl());
        response.setCoverLetter(apply.getCoverLetter());
        response.setJobId(apply.getJob().getId());
        response.setUserId(apply.getUser().getId());
        response.setCreatedAt(apply.getCreatedAt());
        response.setCreatedBy(apply.getCreatedBy());
        response.setUpdatedAt(apply.getUpdatedAt());
        response.setUpdatedBy(apply.getUpdatedBy());

        // Include candidate profile information
        if (apply.getUser() != null && apply.getUser().getProfile() != null) {
            var profile = apply.getUser().getProfile();
            String avatarUrl = profile.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.startsWith("http")) {
                try {
                    avatarUrl = minioService.getFileUrl(avatarUrl);
                } catch (Exception e) {
                    log.warn("Failed to generate presigned URL for avatar: {}", avatarUrl);
                }
            }

            response.setCandidateProfile(ApplyResponse.CandidateProfile.builder()
                    .id(profile.getId())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .email(profile.getEmail())
                    .phoneNumber(profile.getPhoneNumber())
                    .address(profile.getAddress())
                    .avatar(avatarUrl)
                    .dayOfBirth(profile.getDayOfBirth() != null
                            ? profile.getDayOfBirth().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            : null)
                    .description(profile.getDescription())
                    .build());
        }

        return response;
    }

    @Override
    public ListResponse<ApplyResponse> getAppliesByJobId(Long jobId, int pageNo, int pageSize, String sortBy,
            String sortType, Authentication authentication) {
        RoleType currentRole = Helper.getCurrentRole(authentication);

        // Verify job exists
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId.toString()));

        // Verify access
        if (currentRole == RoleType.EMPLOYER) {
            User employer = (User) authentication.getPrincipal();
            if (employer.getCompany() == null) {
                throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
            }
            Long employerCompanyId = employer.getCompany().getId();
            Long jobCompanyId = job.getCompany().getId();
            if (!employerCompanyId.equals(jobCompanyId)) {
                throw new UnauthorizedException("You can only view applications for your company's jobs.");
            }
        }

        Sort sortObj = sortType.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<Apply> pageApply = applyRepository.findByJobId(jobId, pageable);
        List<Apply> applies = pageApply.getContent();

        return ListResponse.<ApplyResponse>builder()
                .data(mapToApplyList(applies))
                .pageNo(pageApply.getNumber())
                .pageSize(pageApply.getSize())
                .totalElements((int) pageApply.getTotalElements())
                .totalPages(pageApply.getTotalPages())
                .isLast(pageApply.isLast())
                .build();
    }

    @Override
    public ListResponse<ApplyResponse> getAppliesByJobIdAndStatus(Long jobId, StatusJob status, int pageNo,
            int pageSize, String sortBy, String sortType, Authentication authentication) {
        RoleType currentRole = Helper.getCurrentRole(authentication);

        // Verify job exists
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId.toString()));

        // Verify access
        if (currentRole == RoleType.EMPLOYER) {
            User employer = (User) authentication.getPrincipal();
            if (employer.getCompany() == null) {
                throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
            }
            Long employerCompanyId = employer.getCompany().getId();
            Long jobCompanyId = job.getCompany().getId();
            if (!employerCompanyId.equals(jobCompanyId)) {
                throw new UnauthorizedException("You can only view applications for your company's jobs.");
            }
        }

        Sort sortObj = sortType.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<Apply> pageApply = applyRepository.findByJobIdAndStatus(jobId, status, pageable);
        List<Apply> applies = pageApply.getContent();

        return ListResponse.<ApplyResponse>builder()
                .data(mapToApplyList(applies))
                .pageNo(pageApply.getNumber())
                .pageSize(pageApply.getSize())
                .totalElements((int) pageApply.getTotalElements())
                .totalPages(pageApply.getTotalPages())
                .isLast(pageApply.isLast())
                .build();
    }

    @Override
    public Map<String, List<ApplyResponse>> getAppliesGroupedByStatus(Long jobId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        verifyApplicationAccess(currentUser);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId.toString()));

        verifyEmployerJobAccess(currentUser, job);

        List<com.cnpm.bottomcv.dto.response.StatusColumnResponse> statusColumns = statusColumnService
                .getAllStatusColumns(jobId, authentication);

        List<Apply> allApplies = applyRepository.findByJobIdOrderByPositionAscCreatedAtDesc(jobId);

        Map<String, List<ApplyResponse>> grouped = initializeGroupedMap(statusColumns);
        groupApplicationsByColumn(allApplies, grouped);

        return grouped;
    }

    private Map<String, List<ApplyResponse>> initializeGroupedMap(
            List<com.cnpm.bottomcv.dto.response.StatusColumnResponse> statusColumns) {
        Map<String, List<ApplyResponse>> grouped = new HashMap<>();
        for (com.cnpm.bottomcv.dto.response.StatusColumnResponse column : statusColumns) {
            grouped.put(column.getCode(), new ArrayList<>());
        }
        return grouped;
    }

    private void groupApplicationsByColumn(List<Apply> allApplies, Map<String, List<ApplyResponse>> grouped) {
        for (Apply apply : allApplies) {
            String columnCode = getColumnCode(apply);
            grouped.computeIfAbsent(columnCode, k -> new ArrayList<>())
                    .add(mapToResponse(apply));
        }
    }

    @Override
    @Transactional
    public ApplyResponse updateApplicationStatus(Long id, UpdateApplicationStatusRequest request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Apply apply = applyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_APPLY_ID_LABEL,
                        AppConstant.FIELD_APPLY_ID, id.toString()));

        verifyApplicationAccess(currentUser);
        verifyEmployerJobAccess(currentUser, apply.getJob());

        StatusJob oldStatus = apply.getStatus();
        StatusJob newStatus = request.getStatus();
        Long jobId = apply.getJob().getId();
        Integer targetPosition = request.getPosition();

        updateApplicationPosition(apply, jobId, oldStatus, newStatus, targetPosition);

        apply.setStatus(newStatus);
        apply.setUpdatedAt(LocalDateTime.now());
        apply.setUpdatedBy(authentication.getName());
        applyRepository.save(apply);

        return mapToResponse(apply);
    }

    private void updateApplicationPosition(Apply apply, Long jobId, StatusJob oldStatus, StatusJob newStatus,
            Integer targetPosition) {
        if (oldStatus.equals(newStatus)) {
            handleSameColumnReorder(jobId, newStatus, apply.getId(), targetPosition, apply);
        } else {
            handleDifferentColumnMove(apply, jobId, oldStatus, newStatus, targetPosition);
        }
    }

    private void handleSameColumnReorder(Long jobId, StatusJob status, Long applicationId, Integer targetPosition,
            Apply apply) {
        if (targetPosition != null) {
            reorderWithinColumn(jobId, status, applicationId, targetPosition);
            apply.setPosition(targetPosition);
        }
    }

    private void handleDifferentColumnMove(Apply apply, Long jobId, StatusJob oldStatus, StatusJob newStatus,
            Integer targetPosition) {
        if (apply.getPosition() != null) {
            shiftPositionsInColumn(jobId, oldStatus, apply.getPosition(), -1);
        }

        if (targetPosition != null) {
            shiftPositionsInColumn(jobId, newStatus, targetPosition, 1);
            apply.setPosition(targetPosition);
        } else {
            apply.setPosition(calculateEndPosition(jobId, newStatus));
        }
    }

    /**
     * Reorder applications within the same column
     */
    private void reorderWithinColumn(Long jobId, StatusJob status, Long applicationId, Integer newPosition) {
        List<Apply> applications = applyRepository.findByJobIdAndStatusOrderByPositionAsc(jobId, status);

        // Find current position
        Apply currentApp = applications.stream()
                .filter(a -> a.getId().equals(applicationId))
                .findFirst()
                .orElse(null);

        if (currentApp == null)
            return;

        Integer oldPosition = currentApp.getPosition() != null ? currentApp.getPosition() : 0;

        if (oldPosition.equals(newPosition)) {
            return; // No change needed
        }

        // Shift other applications
        if (newPosition > oldPosition) {
            // Moving down - shift items between old and new position up
            applications.stream()
                    .filter(a -> !a.getId().equals(applicationId))
                    .filter(a -> a.getPosition() != null && a.getPosition() > oldPosition
                            && a.getPosition() <= newPosition)
                    .forEach(a -> {
                        a.setPosition(a.getPosition() - 1);
                        applyRepository.save(a);
                    });
        } else {
            // Moving up - shift items between new and old position down
            applications.stream()
                    .filter(a -> !a.getId().equals(applicationId))
                    .filter(a -> a.getPosition() != null && a.getPosition() >= newPosition
                            && a.getPosition() < oldPosition)
                    .forEach(a -> {
                        a.setPosition(a.getPosition() + 1);
                        applyRepository.save(a);
                    });
        }
    }

    /**
     * Shift positions in a column (used when moving between columns or removing
     * items)
     */
    private void shiftPositionsInColumn(Long jobId, StatusJob status, Integer fromPosition, int shiftAmount) {
        List<Apply> applications = applyRepository.findByJobIdAndStatusOrderByPositionAsc(jobId, status);

        applications.stream()
                .filter(a -> a.getPosition() != null && a.getPosition() >= fromPosition)
                .forEach(a -> {
                    a.setPosition(a.getPosition() + shiftAmount);
                    applyRepository.save(a);
                });
    }

    @Override
    public org.springframework.core.io.Resource downloadApplicationCV(Long applicationId,
            Authentication authentication) {
        RoleType currentRole = Helper.getCurrentRole(authentication);
        User currentUser = (User) authentication.getPrincipal();

        // Get application
        Apply apply = applyRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Apply", "id", applicationId.toString()));

        // Verify access
        if (currentRole == RoleType.CANDIDATE) {
            // Candidates can only download their own CV
            if (!apply.getUser().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("You can only download your own CV.");
            }
        } else if (currentRole == RoleType.EMPLOYER) {
            // Employers can only download CVs for applications to their company's jobs
            if (currentUser.getCompany() == null) {
                throw new ResourceNotFoundException("Company", "user", currentUser.getId().toString());
            }
            if (!apply.getJob().getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new UnauthorizedException("You can only download CVs for applications to your company's jobs.");
            }
        } else if (currentRole != RoleType.ADMIN) {
            throw new UnauthorizedException("You do not have permission to download this CV.");
        }

        // Check if CV exists
        if (apply.getCvUrl() == null || apply.getCvUrl().isEmpty()) {
            throw new ResourceNotFoundException("CV", "application", applicationId.toString());
        }

        // Download file from MinIO
        try {
            java.io.InputStream inputStream = minioService.downloadFile(apply.getCvUrl());
            return new org.springframework.core.io.InputStreamResource(inputStream);
        } catch (Exception e) {
            log.error("Error downloading CV for application {}: {}", applicationId, e.getMessage(), e);
            throw new RuntimeException("Failed to download CV file", e);
        }
    }

    @Override
    public String getApplicationCVFilename(Long applicationId, Authentication authentication) {
        RoleType currentRole = Helper.getCurrentRole(authentication);
        User currentUser = (User) authentication.getPrincipal();

        // Get application
        Apply apply = applyRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Apply", "id", applicationId.toString()));

        // Verify access (same as downloadApplicationCV)
        if (currentRole == RoleType.CANDIDATE) {
            if (!apply.getUser().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("You can only access your own CV.");
            }
        } else if (currentRole == RoleType.EMPLOYER) {
            if (currentUser.getCompany() == null) {
                throw new ResourceNotFoundException("Company", "user", currentUser.getId().toString());
            }
            if (!apply.getJob().getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new UnauthorizedException("You can only access CVs for applications to your company's jobs.");
            }
        } else if (currentRole != RoleType.ADMIN) {
            throw new UnauthorizedException("You do not have permission to access this CV.");
        }

        if (apply.getCvUrl() == null || apply.getCvUrl().isEmpty()) {
            return "CV.pdf";
        }

        // Extract filename from cvUrl
        String cvUrl = apply.getCvUrl();
        return cvUrl.substring(cvUrl.lastIndexOf("/") + 1);
    }

    // Helper methods to reduce cognitive complexity

    /**
     * Check if user has ADMIN role
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ADMIN);
    }

    /**
     * Check if user has EMPLOYER role
     */
    private boolean hasEmployerRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.EMPLOYER);
    }

    /**
     * Verify that employer user owns the job's company
     */
    private void verifyEmployerOwnsJob(User employer, Job job) {
        if (employer.getCompany() == null) {
            throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
        }
        if (!job.getCompany().getId().equals(employer.getCompany().getId())) {
            throw new UnauthorizedException("You can only access applications for your company's jobs.");
        }
    }

    /**
     * Verify user has permission to access applications (ADMIN or EMPLOYER)
     */
    private void verifyApplicationAccess(User user) {
        boolean hasAdminRole = hasAdminRole(user);
        boolean hasEmployerRole = hasEmployerRole(user);
        if (!hasAdminRole && !hasEmployerRole) {
            throw new UnauthorizedException("Only ADMIN and EMPLOYER can access applications.");
        }
    }

    /**
     * Verify employer can access job's applications
     */
    private void verifyEmployerJobAccess(User employer, Job job) {
        boolean hasAdminRole = hasAdminRole(employer);
        boolean hasEmployerRole = hasEmployerRole(employer);
        if (hasEmployerRole && !hasAdminRole) {
            verifyEmployerOwnsJob(employer, job);
        }
    }

    /**
     * Get column code from apply (with fallback to status)
     */
    private String getColumnCode(Apply apply) {
        if (apply.getStatusColumn() != null) {
            return apply.getStatusColumn().getCode();
        }
        // Fallback: use status enum if statusColumn is null (backward compatibility)
        return apply.getStatus() != null ? apply.getStatus().name() : "UNKNOWN";
    }

    /**
     * Calculate new position when adding to end of column
     */
    private int calculateEndPosition(Long jobId, StatusJob status) {
        List<Apply> existingInColumn = applyRepository.findByJobIdAndStatusOrderByPositionAsc(jobId, status);
        return existingInColumn.stream()
                .mapToInt(a -> a.getPosition() != null ? a.getPosition() : -1)
                .max()
                .orElse(-1) + 1;
    }
}