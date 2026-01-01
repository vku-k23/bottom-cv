package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.constant.EmailTemplateType;
import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "email_templates")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailTemplateType type;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company; // null means global template

    // Placeholders: {{candidate_name}}, {{job_title}}, {{company_name}}, 
    // {{interview_date}}, {{interview_time}}, {{interview_location}}, 
    // {{salary}}, {{start_date}}, {{employer_name}}, {{employer_email}}
}

