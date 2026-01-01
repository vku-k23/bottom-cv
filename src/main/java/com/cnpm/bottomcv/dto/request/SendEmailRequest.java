package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.EmailTemplateType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendEmailRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    private List<String> cc;

    private List<String> bcc;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Template type is required")
    private EmailTemplateType templateType;

    private Long templateId; // Optional: specific template to use

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    private List<String> attachmentUrls; // URLs of uploaded attachments
}
