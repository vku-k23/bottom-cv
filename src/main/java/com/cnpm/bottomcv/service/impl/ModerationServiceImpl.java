package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.AppConstant;

import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.request.BulkModerationRequest;
import com.cnpm.bottomcv.dto.request.ModerationRequest;
import com.cnpm.bottomcv.dto.response.JobResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ModerationQueueResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Job;
import com.cnpm.bottomcv.repository.JobRepository;
import com.cnpm.bottomcv.repository.ReportRepository;
import com.cnpm.bottomcv.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationServiceImpl implements ModerationService {

    private final JobRepository jobRepository;
    private final ReportRepository reportRepository;

    @Override
    public ListResponse<ModerationQueueResponse> getModerationQueue(
            StatusJob status, int pageNo, int pageSize) {
        log.info("Getting moderation queue with status: {}", status);

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());

        Page<Job> jobPage;
        if (status != null) {
            jobPage = jobRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get(AppConstant.FIELD_STATUS), status),
                    pageable);
        } else {
            // Get all jobs with PENDING or other statuses that need moderation
            jobPage = jobRepository.findAll(
                    (root, query, cb) -> cb.or(
                            cb.equal(root.get(AppConstant.FIELD_STATUS), StatusJob.PENDING),
                            cb.equal(root.get(AppConstant.FIELD_STATUS), StatusJob.INACTIVE)),
                    pageable);
        }

        List<ModerationQueueResponse> data = jobPage.getContent().stream()
                .map(this::mapToModerationQueueResponse)
                .collect(Collectors.toList());

        return ListResponse.<ModerationQueueResponse>builder()
                .data(data)
                .pageNo(jobPage.getNumber())
                .pageSize(jobPage.getSize())
                .totalElements((int) jobPage.getTotalElements())
                .totalPages(jobPage.getTotalPages())
                .isLast(jobPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public JobResponse approveJob(Long jobId, ModerationRequest request) {
        log.info("Approving job ID: {}", jobId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId.toString()));

        job.setStatus(StatusJob.ACTIVE);
        Job savedJob = jobRepository.save(job);

        log.info("Successfully approved job ID: {}", jobId);
        return mapToJobResponse(savedJob);
    }

    @Override
    @Transactional
    public JobResponse rejectJob(Long jobId, ModerationRequest request) {
        log.info("Rejecting job ID: {} with reason: {}", jobId, request.getReason());

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId.toString()));

        job.setStatus(StatusJob.INACTIVE);
        Job savedJob = jobRepository.save(job);

        // In a real system, you would:
        // 1. Send email to employer with rejection reason
        // 2. Store rejection reason in a separate table

        log.info("Successfully rejected job ID: {}", jobId);
        return mapToJobResponse(savedJob);
    }

    @Override
    @Transactional
    public void bulkApproveJobs(BulkModerationRequest request) {
        log.info("Bulk approving {} jobs", request.getJobIds().size());

        List<Job> jobs = jobRepository.findAllById(request.getJobIds());
        jobs.forEach(job -> job.setStatus(StatusJob.ACTIVE));
        jobRepository.saveAll(jobs);

        log.info("Successfully bulk approved {} jobs", jobs.size());
    }

    @Override
    @Transactional
    public void bulkRejectJobs(BulkModerationRequest request) {
        log.info("Bulk rejecting {} jobs", request.getJobIds().size());

        List<Job> jobs = jobRepository.findAllById(request.getJobIds());
        jobs.forEach(job -> job.setStatus(StatusJob.INACTIVE));
        jobRepository.saveAll(jobs);

        log.info("Successfully bulk rejected {} jobs", jobs.size());
    }

    @Override
    public void approveReview(Long reviewId) {
        log.info("Approving review ID: {}", reviewId);
        // Placeholder - Review approval logic would go here
        // In a real system, you'd have a Review entity with a status field
    }

    @Override
    public void rejectReview(Long reviewId, String reason) {
        log.info("Rejecting review ID: {} with reason: {}", reviewId, reason);
        // Placeholder - Review rejection logic would go here
    }

    private ModerationQueueResponse mapToModerationQueueResponse(Job job) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Count reports for this job
        Long reportCount = reportRepository.countByResourceTypeAndResourceId("JOB", job.getId());

        return ModerationQueueResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompany() != null ? job.getCompany().getName() : null)
                .companyId(job.getCompany() != null ? job.getCompany().getId() : null)
                .location(job.getLocation())
                .salary(job.getSalary())
                .status(job.getStatus())
                .jobType(job.getJobType().name())
                .submittedAt(job.getCreatedAt() != null ? job.getCreatedAt().format(formatter) : null)
                .submittedBy(job.getCompany() != null ? job.getCompany().getName() : null)
                .submittedById(job.getCompany() != null ? job.getCompany().getId() : null)
                .reportCount(reportCount.intValue())
                .build();
    }

    private JobResponse mapToJobResponse(Job job) {
        // Simple mapping - reuse from JobServiceImpl or create helper
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .jobDescription(job.getJobDescription())
                .status(job.getStatus())
                .build();
    }
}
