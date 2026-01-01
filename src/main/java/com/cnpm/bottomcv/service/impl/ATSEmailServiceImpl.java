package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.EmailStatus;
import com.cnpm.bottomcv.constant.EmailTemplateType;
import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.dto.request.SendEmailRequest;
import com.cnpm.bottomcv.dto.response.EmailLogResponse;
import com.cnpm.bottomcv.dto.response.EmailTemplateResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.exception.UnauthorizedException;
import com.cnpm.bottomcv.model.*;
import com.cnpm.bottomcv.repository.*;
import com.cnpm.bottomcv.service.ATSEmailService;
import com.cnpm.bottomcv.utils.Helper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ATSEmailServiceImpl implements ATSEmailService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailLogRepository emailLogRepository;
    private final ApplyRepository applyRepository;
    private final ProfileRepository profileRepository;
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.username:noreply@bottomcv.com}")
    private String fromEmail;

    @Value("${bottom-cv.mail.from-name:BottomCV}")
    private String fromName;

    @Override
    public List<EmailTemplateResponse> getAvailableTemplates(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        List<EmailTemplate> templates;
        if (currentUser.getCompany() != null) {
            templates = emailTemplateRepository.findActiveTemplatesForCompany(currentUser.getCompany().getId());
        } else {
            templates = emailTemplateRepository.findGlobalActiveTemplates();
        }
        
        return templates.stream()
                .map(this::mapToTemplateResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmailTemplateResponse getTemplateById(Long templateId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        EmailTemplate template = emailTemplateRepository.findByIdAndIsActiveTrue(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Email Template", "id", templateId.toString()));
        
        // Check access: global templates or templates belonging to user's company
        if (template.getCompany() != null && 
            (currentUser.getCompany() == null || !template.getCompany().getId().equals(currentUser.getCompany().getId()))) {
            throw new UnauthorizedException("You don't have access to this template");
        }
        
        return mapToTemplateResponse(template);
    }

    @Override
    public Map<String, String> renderTemplate(Long templateId, Long applicationId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        EmailTemplate template = emailTemplateRepository.findByIdAndIsActiveTrue(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Email Template", "id", templateId.toString()));
        
        Apply application = applyRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId.toString()));
        
        // Verify the employer owns this application's job
        verifyApplicationAccess(application, currentUser);
        
        Map<String, String> placeholders = buildPlaceholders(application, currentUser);
        
        String renderedSubject = replacePlaceholders(template.getSubject(), placeholders);
        String renderedContent = replacePlaceholders(template.getContent(), placeholders);
        
        Map<String, String> result = new HashMap<>();
        result.put("subject", renderedSubject);
        result.put("content", renderedContent);
        result.put("templateId", templateId.toString());
        result.put("templateName", template.getName());
        
        return result;
    }

    @Override
    @Transactional
    public EmailLogResponse sendEmail(SendEmailRequest request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Helper.checkRole(currentUser, RoleType.EMPLOYER, RoleType.ADMIN);
        
        Apply application = applyRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", request.getApplicationId().toString()));
        
        // Verify the employer owns this application's job
        verifyApplicationAccess(application, currentUser);
        
        // Get template if provided
        EmailTemplate template = null;
        if (request.getTemplateId() != null) {
            template = emailTemplateRepository.findById(request.getTemplateId()).orElse(null);
        }
        
        // Create email log entry
        EmailLog emailLog = EmailLog.builder()
                .sender(currentUser)
                .senderEmail(fromEmail)
                .receiverEmail(request.getTo())
                .ccEmails(request.getCc() != null ? String.join(",", request.getCc()) : null)
                .bccEmails(request.getBcc() != null ? String.join(",", request.getBcc()) : null)
                .subject(request.getSubject())
                .content(request.getContent())
                .templateType(request.getTemplateType())
                .template(template)
                .application(application)
                .candidate(application.getUser())
                .job(application.getJob())
                .status(EmailStatus.PENDING)
                .attachmentUrls(request.getAttachmentUrls() != null ? toJson(request.getAttachmentUrls()) : null)
                .build();
        
        emailLog = emailLogRepository.save(emailLog);
        
        // Send email
        try {
            sendEmailViaSMTP(request, emailLog);
            emailLog.setStatus(EmailStatus.SENT);
            emailLog.setSentAt(LocalDateTime.now());
            log.info("Email sent successfully to {} for application {}", request.getTo(), request.getApplicationId());
        } catch (Exception e) {
            emailLog.setStatus(EmailStatus.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            log.error("Failed to send email to {} for application {}: {}", request.getTo(), request.getApplicationId(), e.getMessage());
        }
        
        emailLog = emailLogRepository.save(emailLog);
        
        return mapToEmailLogResponse(emailLog);
    }

    @Override
    public ListResponse<EmailLogResponse> getEmailLogs(int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        Sort sort = sortType.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        
        Page<EmailLog> emailLogs;
        if (Helper.hasRole(currentUser, RoleType.ADMIN)) {
            emailLogs = emailLogRepository.findAll(pageable);
        } else if (currentUser.getCompany() != null) {
            emailLogs = emailLogRepository.findByCompanyId(currentUser.getCompany().getId(), pageable);
        } else {
            emailLogs = emailLogRepository.findBySenderId(currentUser.getId(), pageable);
        }
        
        List<EmailLogResponse> content = emailLogs.getContent().stream()
                .map(this::mapToEmailLogResponse)
                .collect(Collectors.toList());
        
        return ListResponse.<EmailLogResponse>builder()
                .data(content)
                .pageNo(emailLogs.getNumber())
                .pageSize(emailLogs.getSize())
                .totalElements((int) emailLogs.getTotalElements())
                .totalPages(emailLogs.getTotalPages())
                .isLast(emailLogs.isLast())
                .build();
    }

    @Override
    public List<EmailLogResponse> getEmailLogsByApplication(Long applicationId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        Apply application = applyRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId.toString()));
        
        verifyApplicationAccess(application, currentUser);
        
        Pageable pageable = PageRequest.of(0, 100, Sort.by("createdAt").descending());
        Page<EmailLog> emailLogs = emailLogRepository.findByApplicationId(applicationId, pageable);
        
        return emailLogs.getContent().stream()
                .map(this::mapToEmailLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmailLogResponse retryEmail(Long emailLogId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        EmailLog emailLog = emailLogRepository.findById(emailLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Email Log", "id", emailLogId.toString()));
        
        // Verify access
        if (!emailLog.getSender().getId().equals(currentUser.getId()) && !Helper.hasRole(currentUser, RoleType.ADMIN)) {
            throw new UnauthorizedException("You don't have permission to retry this email");
        }
        
        if (emailLog.getStatus() != EmailStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed emails");
        }
        
        // Retry sending
        SendEmailRequest request = SendEmailRequest.builder()
                .to(emailLog.getReceiverEmail())
                .cc(emailLog.getCcEmails() != null ? Arrays.asList(emailLog.getCcEmails().split(",")) : null)
                .bcc(emailLog.getBccEmails() != null ? Arrays.asList(emailLog.getBccEmails().split(",")) : null)
                .subject(emailLog.getSubject())
                .content(emailLog.getContent())
                .templateType(emailLog.getTemplateType())
                .applicationId(emailLog.getApplication().getId())
                .attachmentUrls(emailLog.getAttachmentUrls() != null ? fromJson(emailLog.getAttachmentUrls()) : null)
                .build();
        
        try {
            sendEmailViaSMTP(request, emailLog);
            emailLog.setStatus(EmailStatus.SENT);
            emailLog.setSentAt(LocalDateTime.now());
            emailLog.setErrorMessage(null);
            log.info("Email retry successful for log {}", emailLogId);
        } catch (Exception e) {
            emailLog.setRetryCount(emailLog.getRetryCount() + 1);
            emailLog.setErrorMessage(e.getMessage());
            log.error("Email retry failed for log {}: {}", emailLogId, e.getMessage());
        }
        
        emailLog = emailLogRepository.save(emailLog);
        return mapToEmailLogResponse(emailLog);
    }

    // Helper methods
    
    private void sendEmailViaSMTP(SendEmailRequest request, EmailLog emailLog) throws MessagingException, java.io.UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail, fromName);
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(request.getContent(), true); // true = HTML content
        
        if (request.getCc() != null && !request.getCc().isEmpty()) {
            helper.setCc(request.getCc().toArray(new String[0]));
        }
        
        if (request.getBcc() != null && !request.getBcc().isEmpty()) {
            helper.setBcc(request.getBcc().toArray(new String[0]));
        }
        
        mailSender.send(message);
    }

    private void verifyApplicationAccess(Apply application, User currentUser) {
        if (Helper.hasRole(currentUser, RoleType.ADMIN)) {
            return; // Admin can access all
        }
        
        if (currentUser.getCompany() == null || application.getJob().getCompany() == null) {
            throw new UnauthorizedException("You don't have access to this application");
        }
        
        if (!currentUser.getCompany().getId().equals(application.getJob().getCompany().getId())) {
            throw new UnauthorizedException("You don't have access to this application");
        }
    }
    
    private Map<String, String> buildPlaceholders(Apply application, User employer) {
        Map<String, String> placeholders = new HashMap<>();
        
        // Candidate info
        Profile candidateProfile = profileRepository.findByUserId(application.getUser().getId()).orElse(null);
        if (candidateProfile != null) {
            placeholders.put("candidate_name", candidateProfile.getFirstName() + " " + candidateProfile.getLastName());
            placeholders.put("candidate_first_name", candidateProfile.getFirstName());
            placeholders.put("candidate_last_name", candidateProfile.getLastName());
            placeholders.put("candidate_email", candidateProfile.getEmail());
        } else {
            placeholders.put("candidate_name", application.getUser().getUsername());
            placeholders.put("candidate_first_name", "");
            placeholders.put("candidate_last_name", "");
            placeholders.put("candidate_email", application.getUser().getUsername());
        }
        
        // Job info
        Job job = application.getJob();
        placeholders.put("job_title", job.getTitle());
        placeholders.put("job_location", job.getLocation() != null ? job.getLocation() : "");
        
        // Company info
        Company company = job.getCompany();
        if (company != null) {
            placeholders.put("company_name", company.getName());
            String companyAddress = "";
            if (company.getAddresses() != null && !company.getAddresses().isEmpty()) {
                companyAddress = company.getAddresses().values().stream().findFirst().orElse("");
            }
            placeholders.put("company_address", companyAddress);
        } else {
            placeholders.put("company_name", "");
            placeholders.put("company_address", "");
        }
        
        // Employer info
        Profile employerProfile = profileRepository.findByUserId(employer.getId()).orElse(null);
        if (employerProfile != null) {
            placeholders.put("employer_name", employerProfile.getFirstName() + " " + employerProfile.getLastName());
            placeholders.put("employer_email", employerProfile.getEmail());
        } else {
            placeholders.put("employer_name", employer.getUsername());
            placeholders.put("employer_email", employer.getUsername());
        }
        
        // Date placeholders
        placeholders.put("current_date", LocalDateTime.now().toLocalDate().toString());
        placeholders.put("interview_date", "[INTERVIEW_DATE]");
        placeholders.put("interview_time", "[INTERVIEW_TIME]");
        placeholders.put("interview_location", "[INTERVIEW_LOCATION]");
        placeholders.put("salary", "[SALARY]");
        placeholders.put("start_date", "[START_DATE]");
        
        return placeholders;
    }
    
    private String replacePlaceholders(String text, Map<String, String> placeholders) {
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
    
    private EmailTemplateResponse mapToTemplateResponse(EmailTemplate template) {
        return EmailTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .type(template.getType())
                .subject(template.getSubject())
                .content(template.getContent())
                .description(template.getDescription())
                .isActive(template.getIsActive())
                .companyId(template.getCompany() != null ? template.getCompany().getId() : null)
                .companyName(template.getCompany() != null ? template.getCompany().getName() : null)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
    
    private EmailLogResponse mapToEmailLogResponse(EmailLog emailLog) {
        // Get candidate name
        String candidateName = null;
        if (emailLog.getCandidate() != null) {
            Profile profile = profileRepository.findByUserId(emailLog.getCandidate().getId()).orElse(null);
            if (profile != null) {
                candidateName = profile.getFirstName() + " " + profile.getLastName();
            } else {
                candidateName = emailLog.getCandidate().getUsername();
            }
        }
        
        // Get sender name
        String senderName = null;
        if (emailLog.getSender() != null) {
            Profile profile = profileRepository.findByUserId(emailLog.getSender().getId()).orElse(null);
            if (profile != null) {
                senderName = profile.getFirstName() + " " + profile.getLastName();
            } else {
                senderName = emailLog.getSender().getUsername();
            }
        }
        
        return EmailLogResponse.builder()
                .id(emailLog.getId())
                .senderId(emailLog.getSender() != null ? emailLog.getSender().getId() : null)
                .senderName(senderName)
                .senderEmail(emailLog.getSenderEmail())
                .receiverEmail(emailLog.getReceiverEmail())
                .ccEmails(emailLog.getCcEmails() != null ? Arrays.asList(emailLog.getCcEmails().split(",")) : null)
                .bccEmails(emailLog.getBccEmails() != null ? Arrays.asList(emailLog.getBccEmails().split(",")) : null)
                .subject(emailLog.getSubject())
                .content(emailLog.getContent())
                .templateType(emailLog.getTemplateType())
                .templateId(emailLog.getTemplate() != null ? emailLog.getTemplate().getId() : null)
                .templateName(emailLog.getTemplate() != null ? emailLog.getTemplate().getName() : null)
                .applicationId(emailLog.getApplication() != null ? emailLog.getApplication().getId() : null)
                .candidateId(emailLog.getCandidate() != null ? emailLog.getCandidate().getId() : null)
                .candidateName(candidateName)
                .jobId(emailLog.getJob() != null ? emailLog.getJob().getId() : null)
                .jobTitle(emailLog.getJob() != null ? emailLog.getJob().getTitle() : null)
                .status(emailLog.getStatus())
                .sentAt(emailLog.getSentAt())
                .errorMessage(emailLog.getErrorMessage())
                .attachmentUrls(emailLog.getAttachmentUrls() != null ? fromJson(emailLog.getAttachmentUrls()) : null)
                .createdAt(emailLog.getCreatedAt())
                .build();
    }
    
    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
    
    private List<String> fromJson(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
