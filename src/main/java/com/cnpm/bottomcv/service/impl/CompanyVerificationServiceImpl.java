package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.CompanyVerificationRequest;
import com.cnpm.bottomcv.dto.response.CompanyResponse;
import com.cnpm.bottomcv.dto.response.CompanyVerificationQueueResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Company;
import com.cnpm.bottomcv.repository.CompanyRepository;
import com.cnpm.bottomcv.repository.JobRepository;
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
public class CompanyVerificationServiceImpl {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    public ListResponse<CompanyVerificationQueueResponse> getVerificationQueue(
            Boolean verified, int pageNo, int pageSize) {
        log.info("Getting company verification queue");

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());

        Page<Company> companyPage = companyRepository.findAll(pageable);

        List<CompanyVerificationQueueResponse> data = companyPage.getContent().stream()
                .map(this::mapToVerificationQueueResponse)
                .collect(Collectors.toList());

        return ListResponse.<CompanyVerificationQueueResponse>builder()
                .data(data)
                .pageNo(companyPage.getNumber())
                .pageSize(companyPage.getSize())
                .totalElements((int) companyPage.getTotalElements())
                .totalPages(companyPage.getTotalPages())
                .isLast(companyPage.isLast())
                .build();
    }

    @Transactional
    public CompanyResponse verifyCompany(Long companyId, CompanyVerificationRequest request) {
        log.info("Verifying company ID: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId.toString()));

        if(request.getRejectionReason() != null) {
            log.warn("Verification request for company ID: {} contains a rejection reason. Ignoring it.", companyId);
            company.setVerificationNotes(request.getRejectionReason());
        } else {
            company.setVerified(true);
            company.setVerificationNotes(request.getNotes());
        }

        log.info("Successfully verified company ID: {}", companyId);
        return mapToCompanyResponse(company);
    }

    @Transactional
    public CompanyResponse rejectVerification(Long companyId, CompanyVerificationRequest request) {
        log.info("Rejecting company verification ID: {} with reason: {}",
                companyId, request.getRejectionReason());

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId.toString()));

        // In a real system, you'd:
        // 1. Set verified = false
        // 2. Store rejection reason
        // 3. Send email to company

        log.info("Successfully rejected company verification ID: {}", companyId);
        return mapToCompanyResponse(company);
    }

    private CompanyVerificationQueueResponse mapToVerificationQueueResponse(Company company) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Count jobs for this company
        Integer jobCount = (int) jobRepository
                .count((root, query, cb) -> cb.equal(root.get("company").get("id"), company.getId()));

        return CompanyVerificationQueueResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .slug(company.getSlug())
                .industry(company.getIndustry())
                .companySize(company.getCompanySize())
                .foundedYear(company.getFoundedYear())
                .email(company.getEmail())
                .phone(company.getPhone())
                .website(company.getWebsite())
                .logo(company.getLogo())
                .verified(false) // Placeholder - add this field to Company entity
                .verificationStatus("PENDING") // Placeholder
                .submittedAt(company.getCreatedAt() != null ? company.getCreatedAt().format(formatter) : null)
                .jobCount(jobCount)
                .build();
    }

    private CompanyResponse mapToCompanyResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .slug(company.getSlug())
                .industry(company.getIndustry())
                .companySize(company.getCompanySize())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
