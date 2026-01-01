package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.EmailStatus;
import com.cnpm.bottomcv.constant.EmailTemplateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailLogResponse {
    
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private String receiverEmail;
    private List<String> ccEmails;
    private List<String> bccEmails;
    private String subject;
    private String content;
    private EmailTemplateType templateType;
    private Long templateId;
    private String templateName;
    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private Long jobId;
    private String jobTitle;
    private EmailStatus status;
    private LocalDateTime sentAt;
    private String errorMessage;
    private List<String> attachmentUrls;
    private LocalDateTime createdAt;
}

