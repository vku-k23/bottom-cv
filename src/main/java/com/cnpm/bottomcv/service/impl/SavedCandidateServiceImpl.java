package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.SavedCandidateRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.SavedCandidateResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.exception.UnauthorizedException;
import com.cnpm.bottomcv.model.Job;
import com.cnpm.bottomcv.model.SavedCandidate;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.JobRepository;
import com.cnpm.bottomcv.repository.SavedCandidateRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.MinioService;
import com.cnpm.bottomcv.service.SavedCandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedCandidateServiceImpl implements SavedCandidateService {

    private final SavedCandidateRepository savedCandidateRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final MinioService minioService;

    @Override
    @Transactional
    public SavedCandidateResponse saveCandidate(SavedCandidateRequest request, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();

        // Check if already saved
        Optional<SavedCandidate> existing = savedCandidateRepository
                .findByEmployerIdAndCandidateIdAndJobId(employer.getId(), request.getCandidateId(), request.getJobId());

        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        // Get candidate
        User candidate = userRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getCandidateId().toString()));

        // Get job
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId().toString()));

        // Create saved candidate
        SavedCandidate savedCandidate = SavedCandidate.builder()
                .employer(employer)
                .candidate(candidate)
                .job(job)
                .note(request.getNote())
                .build();

        savedCandidate.setCreatedBy(employer.getUsername());
        savedCandidate.setUpdatedBy(employer.getUsername());

        savedCandidate = savedCandidateRepository.save(savedCandidate);
        log.info("Employer {} saved candidate {} for job {}", employer.getId(), candidate.getId(), job.getId());

        return mapToResponse(savedCandidate);
    }

    @Override
    @Transactional
    public void removeSavedCandidate(Long id, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();

        SavedCandidate savedCandidate = savedCandidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavedCandidate", "id", id.toString()));

        if (!savedCandidate.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You can only remove your own saved candidates");
        }

        savedCandidateRepository.delete(savedCandidate);
        log.info("Employer {} removed saved candidate {}", employer.getId(), id);
    }

    @Override
    @Transactional
    public void removeSavedCandidateByIds(Long candidateId, Long jobId, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();

        SavedCandidate savedCandidate = savedCandidateRepository
                .findByEmployerIdAndCandidateIdAndJobId(employer.getId(), candidateId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedCandidate",
                        "candidateId/jobId", candidateId + "/" + jobId));

        savedCandidateRepository.delete(savedCandidate);
        log.info("Employer {} removed saved candidate {} for job {}", employer.getId(), candidateId, jobId);
    }

    @Override
    public ListResponse<SavedCandidateResponse> getSavedCandidates(
            int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();

        Sort sort = sortType.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<SavedCandidate> page = savedCandidateRepository.findByEmployerId(employer.getId(), pageable);

        List<SavedCandidateResponse> data = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return ListResponse.<SavedCandidateResponse>builder()
                .data(data)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements((int) page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }

    @Override
    public boolean isCandidateSaved(Long candidateId, Long jobId, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();
        return savedCandidateRepository.existsByEmployerIdAndCandidateIdAndJobId(
                employer.getId(), candidateId, jobId);
    }

    @Override
    @Transactional
    public SavedCandidateResponse toggleSaveCandidate(SavedCandidateRequest request, Authentication authentication) {
        User employer = (User) authentication.getPrincipal();

        Optional<SavedCandidate> existing = savedCandidateRepository
                .findByEmployerIdAndCandidateIdAndJobId(employer.getId(), request.getCandidateId(), request.getJobId());

        if (existing.isPresent()) {
            // Unsave
            savedCandidateRepository.delete(existing.get());
            log.info("Employer {} unsaved candidate {} for job {}",
                    employer.getId(), request.getCandidateId(), request.getJobId());
            return null; // Return null to indicate unsaved
        } else {
            // Save
            return saveCandidate(request, authentication);
        }
    }

    private SavedCandidateResponse mapToResponse(SavedCandidate savedCandidate) {
        User candidate = savedCandidate.getCandidate();
        Job job = savedCandidate.getJob();

        String candidateName = "Unknown";
        String candidateEmail = null;
        String candidatePhone = null;
        String candidateAvatar = null;
        String candidateAddress = null;

        if (candidate.getProfile() != null) {
            var profile = candidate.getProfile();
            candidateName = profile.getFirstName() + " " + profile.getLastName();
            candidateEmail = profile.getEmail();
            candidatePhone = profile.getPhoneNumber();
            candidateAddress = profile.getAddress();

            String avatar = profile.getAvatar();
            if (avatar != null && !avatar.isEmpty() && !avatar.startsWith("http")) {
                try {
                    candidateAvatar = minioService.getFileUrl(avatar);
                } catch (Exception e) {
                    log.warn("Failed to generate presigned URL for avatar: {}", avatar);
                    candidateAvatar = avatar;
                }
            } else {
                candidateAvatar = avatar;
            }
        }

        return SavedCandidateResponse.builder()
                .id(savedCandidate.getId())
                .candidateId(candidate.getId())
                .candidateName(candidateName)
                .candidateEmail(candidateEmail)
                .candidatePhone(candidatePhone)
                .candidateAvatar(candidateAvatar)
                .candidateAddress(candidateAddress)
                .jobId(job != null ? job.getId() : null)
                .jobTitle(job != null ? job.getTitle() : null)
                .note(savedCandidate.getNote())
                .savedAt(savedCandidate.getCreatedAt())
                .build();
    }
}
