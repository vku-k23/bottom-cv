package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.ApplyRequest;
import com.cnpm.bottomcv.dto.response.ApplyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.BadRequestException;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Apply;
import com.cnpm.bottomcv.model.CV;
import com.cnpm.bottomcv.model.Job;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.*;
import com.cnpm.bottomcv.service.ApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplyServiceImpl implements ApplyService {

    private final ApplyRepository applyRepository;
    private final CVRepository cvRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public ApplyResponse createApply(ApplyRequest request) {
        Apply apply = new Apply();
        mapRequestToEntity(apply, request);
        apply.setCreatedAt(LocalDateTime.now());
        apply.setCreatedBy("system");
        applyRepository.save(apply);
        return mapToResponse(apply);
    }

    @Override
    public ApplyResponse getApplyById(Long id) {
        Apply apply = applyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));
        return mapToResponse(apply);
    }

    @Override
    public ListResponse<ApplyResponse> getAllApplies(int pageNo, int pageSize, String sortBy, String sortType) {
        Sort sortObj = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<Apply> pageApply = applyRepository.findAll(pageable);
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

    private List<ApplyResponse> mapToApplyList(List<Apply> applies) {
        return applies.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApplyResponse updateApply(Long id, ApplyRequest request) {
        Apply apply = applyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));

        mapRequestToEntity(apply, request);
        apply.setUpdatedAt(LocalDateTime.now());
        apply.setUpdatedBy("system");
        applyRepository.save(apply);
        return mapToResponse(apply);
    }

    @Override
    public void deleteApply(Long id) {
        Apply apply = applyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apply id", "applyId", id.toString()));
        applyRepository.delete(apply);
    }

    private void mapRequestToEntity(Apply apply, ApplyRequest request) {
        apply.setMessage(request.getMessage());
        apply.setStatus(request.getStatus());

        CV cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new ResourceNotFoundException("CV id", "cvId", request.getCvId().toString()));
        if (!cv.getUser().getId().equals(request.getUserId())) {
            throw new BadRequestException(String.format("CV id %s and user id %s do not match!", request.getCvId(), request.getUserId()));
        }
        apply.setCv(cv);

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job id", "jobId", request.getJobId().toString()));
        apply.setJob(job);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User id", "userId", request.getUserId().toString()));
        apply.setUser(user);
    }

    private ApplyResponse mapToResponse(Apply apply) {
        ApplyResponse response = new ApplyResponse();
        response.setId(apply.getId());
        response.setMessage(apply.getMessage());
        response.setStatus(apply.getStatus());
        response.setCvId(apply.getCv().getId());
        response.setJobId(apply.getJob().getId());
        response.setUserId(apply.getUser().getId());
        response.setCreatedAt(apply.getCreatedAt());
        response.setCreatedBy(apply.getCreatedBy());
        response.setUpdatedAt(apply.getUpdatedAt());
        response.setUpdatedBy(apply.getUpdatedBy());
        return response;
    }
}