package com.cnpm.bottomcv.service.impl;

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
                .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));
        
        switch (currentRole) {
            case CANDIDATE:
                User user = userRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "username", authentication.getName()));
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
    public ListResponse<ApplyResponse> getAllApplies(int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication) {

        RoleType currentRole = Helper.getCurrentRole(authentication);

        switch (currentRole) {
            case CANDIDATE:
                User user = userRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "username", authentication.getName()));
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
                        .orElseThrow(() -> new ResourceNotFoundException("User", "username", authentication.getName()));
                if (!applyRepository.existsByIdAndUserId(id, user.getId())) {
                    throw new ResourceNotFoundException("Apply id", "applyId", id.toString());
                }
                Apply apply = applyRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));

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
                        .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));
                
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
                        .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));
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
    public ApplyResponse submitApplication(Long jobId, String coverLetter, org.springframework.web.multipart.MultipartFile cvFile, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job id", "jobId", jobId.toString()));

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
                        .orElseThrow(() -> new ResourceNotFoundException("User", "username", authentication.getName()));
                if (!applyRepository.existsByIdAndUserId(id, user.getId())) {
                    throw new ResourceNotFoundException("Apply id", "applyId", id.toString());
                }
                applyRepository.deleteById(id);
                break;
            case EMPLOYER:
                User employer = (User) authentication.getPrincipal();
                if (employer.getCompany() == null) {
                    throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
                }
                Apply employerApply = applyRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));
                
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
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        Long userId = user.getId();

        CV cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new ResourceNotFoundException("CV id", "cvId", request.getCvId().toString()));
        if (!cv.getUser().getId().equals(userId)) {
            throw new BadRequestException(String.format("CV id %s and user id %s do not match!", request.getCvId(), userId));
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
        return response;
    }

    @Override
    public ListResponse<ApplyResponse> getAppliesByJobId(Long jobId, int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication) {
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
    public ListResponse<ApplyResponse> getAppliesByJobIdAndStatus(Long jobId, StatusJob status, int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication) {
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
    public Map<StatusJob, List<ApplyResponse>> getAppliesGroupedByStatus(Long jobId, Authentication authentication) {
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
        
        // Get all applications for the job
        List<Apply> applies = applyRepository.findByJobIdOrderByCreatedAtDesc(jobId);
        
        // Group by status
        Map<StatusJob, List<ApplyResponse>> grouped = new HashMap<>();
        for (StatusJob status : StatusJob.values()) {
            grouped.put(status, new java.util.ArrayList<>());
        }
        
        for (Apply apply : applies) {
            StatusJob status = apply.getStatus();
            grouped.get(status).add(mapToResponse(apply));
        }
        
        return grouped;
    }

    @Override
    @Transactional
    public ApplyResponse updateApplicationStatus(Long id, UpdateApplicationStatusRequest request, Authentication authentication) {
        RoleType currentRole = Helper.getCurrentRole(authentication);
        
        Apply apply = applyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));
        
        // Verify access
        if (currentRole == RoleType.EMPLOYER) {
            User employer = (User) authentication.getPrincipal();
            if (employer.getCompany() == null) {
                throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
            }
            Long employerCompanyId = employer.getCompany().getId();
            Long applicationCompanyId = apply.getJob().getCompany().getId();
            if (!employerCompanyId.equals(applicationCompanyId)) {
                throw new UnauthorizedException("You can only update applications for your company's jobs.");
            }
        } else if (currentRole == RoleType.CANDIDATE) {
            throw new BadRequestException("Candidates cannot update application status. Use updateApply instead.");
        }
        
        // Update status
        apply.setStatus(request.getStatus());
        apply.setUpdatedAt(LocalDateTime.now());
        apply.setUpdatedBy(authentication.getName());
        applyRepository.save(apply);
        
        return mapToResponse(apply);
    }
}