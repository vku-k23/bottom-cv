package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.SendEmailRequest;
import com.cnpm.bottomcv.dto.response.EmailLogResponse;
import com.cnpm.bottomcv.dto.response.EmailTemplateResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.ATSEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/admin/emails", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@Tag(name = "Email Management", description = "APIs for sending and managing emails to candidates")
@Slf4j
public class EmailController {

    private final ATSEmailService atsEmailService;

    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get available email templates", description = "Get all email templates available for the current user's company")
    public ResponseEntity<List<EmailTemplateResponse>> getTemplates(Authentication authentication) {
        log.info("Fetching email templates for user: {}", authentication.getName());
        List<EmailTemplateResponse> templates = atsEmailService.getAvailableTemplates(authentication);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/{templateId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get a specific email template", description = "Get details of a specific email template by ID")
    public ResponseEntity<EmailTemplateResponse> getTemplateById(
            @PathVariable Long templateId,
            Authentication authentication) {
        log.info("Fetching email template {} for user: {}", templateId, authentication.getName());
        EmailTemplateResponse template = atsEmailService.getTemplateById(templateId, authentication);
        return ResponseEntity.ok(template);
    }

    @GetMapping("/templates/{templateId}/render")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Render email template with placeholders", description = "Render an email template with placeholders replaced based on application data")
    public ResponseEntity<Map<String, String>> renderTemplate(
            @PathVariable Long templateId,
            @RequestParam Long applicationId,
            Authentication authentication) {
        log.info("Rendering template {} for application {} by user: {}", templateId, applicationId, authentication.getName());
        Map<String, String> rendered = atsEmailService.renderTemplate(templateId, applicationId, authentication);
        return ResponseEntity.ok(rendered);
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Send email to candidate", description = "Send an email to a candidate with the specified content")
    public ResponseEntity<EmailLogResponse> sendEmail(
            @Valid @RequestBody SendEmailRequest request,
            Authentication authentication) {
        log.info("Sending email to {} for application {} by user: {}", 
                request.getTo(), request.getApplicationId(), authentication.getName());
        EmailLogResponse response = atsEmailService.sendEmail(request, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get email logs", description = "Get email logs for the current user or company")
    public ResponseEntity<ListResponse<EmailLogResponse>> getEmailLogs(
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortType", defaultValue = "desc") String sortType,
            Authentication authentication) {
        log.info("Fetching email logs for user: {}", authentication.getName());
        ListResponse<EmailLogResponse> logs = atsEmailService.getEmailLogs(pageNo, pageSize, sortBy, sortType, authentication);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/application/{applicationId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Get email logs for an application", description = "Get all email logs for a specific application")
    public ResponseEntity<List<EmailLogResponse>> getEmailLogsByApplication(
            @PathVariable Long applicationId,
            Authentication authentication) {
        log.info("Fetching email logs for application {} by user: {}", applicationId, authentication.getName());
        List<EmailLogResponse> logs = atsEmailService.getEmailLogsByApplication(applicationId, authentication);
        return ResponseEntity.ok(logs);
    }

    @PostMapping("/logs/{emailLogId}/retry")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    @Operation(summary = "Retry a failed email", description = "Retry sending a failed email")
    public ResponseEntity<EmailLogResponse> retryEmail(
            @PathVariable Long emailLogId,
            Authentication authentication) {
        log.info("Retrying email {} by user: {}", emailLogId, authentication.getName());
        EmailLogResponse response = atsEmailService.retryEmail(emailLogId, authentication);
        return ResponseEntity.ok(response);
    }
}

