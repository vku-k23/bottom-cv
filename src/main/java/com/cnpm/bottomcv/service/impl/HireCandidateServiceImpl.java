package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.AppConstant;

import com.cnpm.bottomcv.constant.ApplicationStatus;
import com.cnpm.bottomcv.constant.EmailTemplateType;
import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.request.HireCandidateRequest;
import com.cnpm.bottomcv.dto.request.SendEmailRequest;
import com.cnpm.bottomcv.dto.response.ApplicationStatusHistoryResponse;
import com.cnpm.bottomcv.dto.response.HireCandidateResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.exception.UnauthorizedException;
import com.cnpm.bottomcv.model.*;
import com.cnpm.bottomcv.repository.*;
import com.cnpm.bottomcv.service.ATSEmailService;
import com.cnpm.bottomcv.service.HireCandidateService;
import com.cnpm.bottomcv.utils.Helper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HireCandidateServiceImpl implements HireCandidateService {

    private final ApplyRepository applyRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final ProfileRepository profileRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final ATSEmailService atsEmailService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public HireCandidateResponse hireCandidate(HireCandidateRequest request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Helper.checkRole(currentUser, RoleType.EMPLOYER, RoleType.ADMIN);
        
        Apply application = applyRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.ENTITY_APPLICATION, "id", request.getApplicationId().toString()));
        
        // Verify access
        verifyApplicationAccess(application, currentUser);
        
        // Get current status for history
        ApplicationStatus previousStatus = mapToApplicationStatus(application.getStatus());
        
        // Check if already hired
        if (previousStatus == ApplicationStatus.HIRED) {
            throw new IllegalStateException("Candidate is already hired for this position");
        }
        
        // Update application status
        application.setStatus(StatusJob.ACTIVE); // Using ACTIVE to represent HIRED in the existing enum
        
        // Create offer details JSON
        String offerDetails = buildOfferDetailsJson(request);
        
        // Save status history
        ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                .application(application)
                .previousStatus(previousStatus)
                .newStatus(ApplicationStatus.HIRED)
                .changedBy(currentUser)
                .changedAt(LocalDateTime.now())
                .note(request.getNote())
                .offerDetails(offerDetails)
                .build();
        
        statusHistoryRepository.save(history);
        applyRepository.save(application);
        
        // Get candidate profile
        Profile candidateProfile = profileRepository.findByUserId(application.getUser().getId()).orElse(null);
        String candidateName = candidateProfile != null 
                ? candidateProfile.getFirstName() + " " + candidateProfile.getLastName()
                : application.getUser().getUsername();
        String candidateEmail = candidateProfile != null 
                ? candidateProfile.getEmail() 
                : application.getUser().getUsername();
        
        // Send offer email if requested
        boolean emailSent = false;
        if (Boolean.TRUE.equals(request.getSendOfferEmail())) {
            try {
                sendOfferEmail(application, request, candidateEmail, authentication);
                emailSent = true;
                log.info("Offer email sent to candidate {}", candidateEmail);
            } catch (Exception e) {
                log.error("Failed to send offer email: {}", e.getMessage());
            }
        }
        
        // Get employer name
        Profile employerProfile = profileRepository.findByUserId(currentUser.getId()).orElse(null);
        String hiredByName = employerProfile != null 
                ? employerProfile.getFirstName() + " " + employerProfile.getLastName()
                : currentUser.getUsername();
        
        log.info("Candidate {} hired for job {} by {}", candidateName, application.getJob().getTitle(), hiredByName);
        
        return HireCandidateResponse.builder()
                .applicationId(application.getId())
                .candidateId(application.getUser().getId())
                .candidateName(candidateName)
                .candidateEmail(candidateEmail)
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .previousStatus(previousStatus)
                .newStatus(ApplicationStatus.HIRED)
                .hiredAt(history.getChangedAt())
                .hiredBy(hiredByName)
                .salary(request.getSalary())
                .salaryCurrency(request.getSalaryCurrency())
                .startDate(request.getStartDate())
                .position(request.getPosition())
                .department(request.getDepartment())
                .contractType(request.getContractType())
                .message("Candidate hired successfully")
                .emailSent(emailSent)
                .build();
    }

    @Override
    public List<ApplicationStatusHistoryResponse> getApplicationStatusHistory(Long applicationId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        Apply application = applyRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.ENTITY_APPLICATION, "id", applicationId.toString()));
        
        verifyApplicationAccess(application, currentUser);
        
        List<ApplicationStatusHistory> histories = statusHistoryRepository.findByApplicationIdOrderByChangedAtDesc(applicationId);
        
        return histories.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApplicationStatusHistoryResponse updateApplicationStatus(Long applicationId, String newStatusStr, String note, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Helper.checkRole(currentUser, RoleType.EMPLOYER, RoleType.ADMIN);
        
        Apply application = applyRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.ENTITY_APPLICATION, "id", applicationId.toString()));
        
        verifyApplicationAccess(application, currentUser);
        
        ApplicationStatus previousStatus = mapToApplicationStatus(application.getStatus());
        ApplicationStatus newStatus;
        
        try {
            newStatus = ApplicationStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatusStr);
        }
        
        // Map ApplicationStatus to StatusJob
        StatusJob statusJob = mapToStatusJob(newStatus);
        application.setStatus(statusJob);
        
        // Save history
        ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                .application(application)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .changedBy(currentUser)
                .changedAt(LocalDateTime.now())
                .note(note)
                .build();
        
        statusHistoryRepository.save(history);
        applyRepository.save(application);
        
        log.info("Application {} status changed from {} to {} by {}", 
                applicationId, previousStatus, newStatus, currentUser.getUsername());
        
        return mapToHistoryResponse(history);
    }

    // Helper methods
    
    private void verifyApplicationAccess(Apply application, User currentUser) {
        if (Helper.hasRole(currentUser, RoleType.ADMIN)) {
            return;
        }
        
        if (currentUser.getCompany() == null || application.getJob().getCompany() == null) {
            throw new UnauthorizedException("You don't have access to this application");
        }
        
        if (!currentUser.getCompany().getId().equals(application.getJob().getCompany().getId())) {
            throw new UnauthorizedException("You don't have access to this application");
        }
    }
    
    private ApplicationStatus mapToApplicationStatus(StatusJob statusJob) {
        if (statusJob == null) return ApplicationStatus.NEW;
        return switch (statusJob) {
            case ACTIVE -> ApplicationStatus.HIRED;
            case INACTIVE -> ApplicationStatus.REJECTED;
            case PENDING -> ApplicationStatus.SCREENING;
        };
    }
    
    private StatusJob mapToStatusJob(ApplicationStatus status) {
        return switch (status) {
            case HIRED -> StatusJob.ACTIVE;
            case REJECTED, WITHDRAWN -> StatusJob.INACTIVE;
            default -> StatusJob.PENDING;
        };
    }
    
    private String buildOfferDetailsJson(HireCandidateRequest request) {
        Map<String, Object> offerDetails = new HashMap<>();
        if (request.getSalary() != null) offerDetails.put("salary", request.getSalary());
        if (request.getSalaryCurrency() != null) offerDetails.put("salaryCurrency", request.getSalaryCurrency());
        if (request.getStartDate() != null) offerDetails.put("startDate", request.getStartDate().toString());
        if (request.getPosition() != null) offerDetails.put("position", request.getPosition());
        if (request.getDepartment() != null) offerDetails.put("department", request.getDepartment());
        if (request.getContractType() != null) offerDetails.put("contractType", request.getContractType());
        if (request.getAdditionalBenefits() != null) offerDetails.put("additionalBenefits", request.getAdditionalBenefits());
        
        try {
            return objectMapper.writeValueAsString(offerDetails);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
    
    private void sendOfferEmail(Apply application, HireCandidateRequest request, String candidateEmail, Authentication authentication) {
        // Find offer template
        List<EmailTemplate> offerTemplates = emailTemplateRepository.findByTypeAndIsActiveTrue(EmailTemplateType.OFFER);
        
        String subject = "Job Offer - " + application.getJob().getTitle();
        String content = buildOfferEmailContent(application, request);
        
        if (!offerTemplates.isEmpty()) {
            EmailTemplate template = offerTemplates.get(0);
            subject = template.getSubject();
            content = template.getContent();
        }
        
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to(candidateEmail)
                .subject(subject)
                .content(content)
                .templateType(EmailTemplateType.OFFER)
                .applicationId(application.getId())
                .build();
        
        atsEmailService.sendEmail(emailRequest, authentication);
    }
    
    private String buildOfferEmailContent(Apply application, HireCandidateRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>Congratulations!</h2>");
        sb.append("<p>We are pleased to offer you the position of <strong>")
          .append(application.getJob().getTitle())
          .append("</strong> at <strong>")
          .append(application.getJob().getCompany() != null ? application.getJob().getCompany().getName() : "our company")
          .append("</strong>.</p>");
        
        if (request.getSalary() != null) {
            sb.append("<p><strong>Salary:</strong> ")
              .append(request.getSalary())
              .append(" ")
              .append(request.getSalaryCurrency() != null ? request.getSalaryCurrency() : "")
              .append("</p>");
        }
        
        if (request.getStartDate() != null) {
            sb.append("<p><strong>Start Date:</strong> ")
              .append(request.getStartDate())
              .append("</p>");
        }
        
        if (request.getDepartment() != null) {
            sb.append("<p><strong>Department:</strong> ")
              .append(request.getDepartment())
              .append("</p>");
        }
        
        if (request.getContractType() != null) {
            sb.append("<p><strong>Contract Type:</strong> ")
              .append(request.getContractType())
              .append("</p>");
        }
        
        sb.append("<p>Please confirm your acceptance of this offer at your earliest convenience.</p>");
        sb.append("<p>Best regards,<br/>The Hiring Team</p>");
        sb.append("</body></html>");
        
        return sb.toString();
    }
    
    private ApplicationStatusHistoryResponse mapToHistoryResponse(ApplicationStatusHistory history) {
        String changedByName = null;
        if (history.getChangedBy() != null) {
            Profile profile = profileRepository.findByUserId(history.getChangedBy().getId()).orElse(null);
            if (profile != null) {
                changedByName = profile.getFirstName() + " " + profile.getLastName();
            } else {
                changedByName = history.getChangedBy().getUsername();
            }
        }
        
        return ApplicationStatusHistoryResponse.builder()
                .id(history.getId())
                .applicationId(history.getApplication().getId())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .changedById(history.getChangedBy() != null ? history.getChangedBy().getId() : null)
                .changedByName(changedByName)
                .changedAt(history.getChangedAt())
                .note(history.getNote())
                .offerDetails(history.getOfferDetails())
                .build();
    }
}

