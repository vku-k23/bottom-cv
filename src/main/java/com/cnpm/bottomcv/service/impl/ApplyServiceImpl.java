package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.dto.request.ApplyRequest;
import com.cnpm.bottomcv.dto.response.ApplyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.BadRequestException;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
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
import java.util.List;
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
        switch (currentRole) {
            case CANDIDATE:
                User user = userRepository.findByUsername(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "username", authentication.getName()));
                Apply apply = applyRepository.findByIdAndUserId(id, user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));
                return mapToResponse(apply);
            case EMPLOYER:
                // TODO: Implement logic to allow employers to view applications for their jobs
                break;
            case ADMIN:
                // TODO: Admins can view any application
                break;
            default:
                throw new BadRequestException("Invalid role for viewing an application.");
        }
        return null;
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
                // TODO: Implement logic to allow employers to view applications for their jobs
                break;
            case ADMIN:
                // TODO: Admins can view all applications
                break;
            default:
                throw new BadRequestException("You do not have permission to view applications.");
        }
        return ListResponse.<ApplyResponse>builder()
                .data(List.of())
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(0)
                .totalPages(0)
                .isLast(true)
                .build();
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
            case EMPLOYER, ADMIN:
                throw new BadRequestException("Only candidates can update their applications.");
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
            case EMPLOYER, ADMIN:
                //TODO: Employers and admins can delete any application
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
}