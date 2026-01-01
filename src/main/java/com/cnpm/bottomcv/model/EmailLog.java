package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.constant.EmailStatus;
import com.cnpm.bottomcv.constant.EmailTemplateType;
import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "email_logs")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    @Column(name = "receiver_email", nullable = false)
    private String receiverEmail;

    @Column(name = "cc_emails")
    private String ccEmails; // Comma-separated list

    @Column(name = "bcc_emails")
    private String bccEmails; // Comma-separated list

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type")
    private EmailTemplateType templateType;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private EmailTemplate template;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Apply application;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private User candidate;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "attachment_urls", columnDefinition = "TEXT")
    private String attachmentUrls; // JSON array of attachment URLs

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
}
