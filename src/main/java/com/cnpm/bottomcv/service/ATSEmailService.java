package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.SendEmailRequest;
import com.cnpm.bottomcv.dto.response.EmailLogResponse;
import com.cnpm.bottomcv.dto.response.EmailTemplateResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

/**
 * Service for ATS (Applicant Tracking System) email functionality
 * Handles candidate communication emails like interview invitations, offers, rejections
 */
public interface ATSEmailService {
    
    /**
     * Get all available email templates for the current user's company
     */
    List<EmailTemplateResponse> getAvailableTemplates(Authentication authentication);
    
    /**
     * Get a specific email template by ID
     */
    EmailTemplateResponse getTemplateById(Long templateId, Authentication authentication);
    
    /**
     * Render template with placeholders replaced
     */
    Map<String, String> renderTemplate(Long templateId, Long applicationId, Authentication authentication);
    
    /**
     * Send an email to a candidate
     */
    EmailLogResponse sendEmail(SendEmailRequest request, Authentication authentication);
    
    /**
     * Get email logs for the current user
     */
    ListResponse<EmailLogResponse> getEmailLogs(int pageNo, int pageSize, String sortBy, String sortType, Authentication authentication);
    
    /**
     * Get email logs for a specific application
     */
    List<EmailLogResponse> getEmailLogsByApplication(Long applicationId, Authentication authentication);
    
    /**
     * Retry sending a failed email
     */
    EmailLogResponse retryEmail(Long emailLogId, Authentication authentication);
}

