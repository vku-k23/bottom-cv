package com.cnpm.bottomcv.config;

import com.cnpm.bottomcv.constant.EmailTemplateType;
import com.cnpm.bottomcv.model.EmailTemplate;
import com.cnpm.bottomcv.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after other data loaders
public class EmailTemplateDataLoader implements CommandLineRunner {

    private final EmailTemplateRepository emailTemplateRepository;

    @Override
    public void run(String... args) {
        if (emailTemplateRepository.count() == 0) {
            log.info("Loading default email templates...");
            createDefaultTemplates();
            log.info("Default email templates loaded successfully.");
        }
    }

    private void createDefaultTemplates() {
        List<EmailTemplate> templates = List.of(
                // Interview Invitation Template
                EmailTemplate.builder()
                        .name("Interview Invitation")
                        .type(EmailTemplateType.INTERVIEW)
                        .subject("Interview Invitation for {{job_title}} at {{company_name}}")
                        .content("""
                                <html>
                                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                                    <h2 style="color: #0A65CC;">Interview Invitation</h2>
                                    
                                    <p>Dear {{candidate_name}},</p>
                                    
                                    <p>Thank you for applying for the <strong>{{job_title}}</strong> position at <strong>{{company_name}}</strong>. After reviewing your application, we are pleased to invite you for an interview.</p>
                                    
                                    <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                        <h3 style="margin-top: 0; color: #0A65CC;">Interview Details</h3>
                                        <p><strong>Date:</strong> {{interview_date}}</p>
                                        <p><strong>Time:</strong> {{interview_time}}</p>
                                        <p><strong>Location:</strong> {{interview_location}}</p>
                                    </div>
                                    
                                    <p>Please confirm your attendance by replying to this email at your earliest convenience. If the proposed time doesn't work for you, please let us know your availability so we can reschedule.</p>
                                    
                                    <p>If you have any questions, feel free to reach out to us.</p>
                                    
                                    <p>We look forward to meeting you!</p>
                                    
                                    <p>Best regards,<br/>
                                    <strong>{{employer_name}}</strong><br/>
                                    {{company_name}}<br/>
                                    {{employer_email}}</p>
                                </body>
                                </html>
                                """)
                        .description("Standard template for inviting candidates to interviews")
                        .isActive(true)
                        .build(),

                // Job Offer Template
                EmailTemplate.builder()
                        .name("Job Offer")
                        .type(EmailTemplateType.OFFER)
                        .subject("Job Offer: {{job_title}} at {{company_name}}")
                        .content("""
                                <html>
                                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                                    <h2 style="color: #0A65CC;">🎉 Congratulations!</h2>
                                    
                                    <p>Dear {{candidate_name}},</p>
                                    
                                    <p>We are thrilled to inform you that you have been selected for the position of <strong>{{job_title}}</strong> at <strong>{{company_name}}</strong>!</p>
                                    
                                    <p>After careful consideration, we believe your skills, experience, and enthusiasm make you an excellent fit for our team.</p>
                                    
                                    <div style="background-color: #e7f0fa; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #0A65CC;">
                                        <h3 style="margin-top: 0; color: #0A65CC;">Offer Details</h3>
                                        <p><strong>Position:</strong> {{job_title}}</p>
                                        <p><strong>Salary:</strong> {{salary}}</p>
                                        <p><strong>Start Date:</strong> {{start_date}}</p>
                                        <p><strong>Location:</strong> {{company_address}}</p>
                                    </div>
                                    
                                    <p>Please review the offer and let us know your decision by replying to this email. If you have any questions or would like to discuss any aspect of the offer, we're happy to arrange a call.</p>
                                    
                                    <p>We're excited about the possibility of you joining our team and contributing to our success!</p>
                                    
                                    <p>Warm regards,<br/>
                                    <strong>{{employer_name}}</strong><br/>
                                    {{company_name}}<br/>
                                    {{employer_email}}</p>
                                </body>
                                </html>
                                """)
                        .description("Standard template for extending job offers to candidates")
                        .isActive(true)
                        .build(),

                // Rejection Template
                EmailTemplate.builder()
                        .name("Application Rejection")
                        .type(EmailTemplateType.REJECTION)
                        .subject("Update on Your Application for {{job_title}} at {{company_name}}")
                        .content("""
                                <html>
                                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                                    <h2 style="color: #666;">Application Update</h2>
                                    
                                    <p>Dear {{candidate_name}},</p>
                                    
                                    <p>Thank you for your interest in the <strong>{{job_title}}</strong> position at <strong>{{company_name}}</strong> and for taking the time to apply.</p>
                                    
                                    <p>After careful consideration, we have decided to move forward with other candidates whose qualifications more closely match our current needs. This was a difficult decision, as we received many impressive applications.</p>
                                    
                                    <p>We want to encourage you to apply for future positions at {{company_name}} that match your skills and interests. We were impressed by your background and believe you have much to offer.</p>
                                    
                                    <p>We appreciate your interest in joining our team and wish you all the best in your job search and future career endeavors.</p>
                                    
                                    <p>Best regards,<br/>
                                    <strong>{{employer_name}}</strong><br/>
                                    {{company_name}}<br/>
                                    {{employer_email}}</p>
                                </body>
                                </html>
                                """)
                        .description("Professional template for rejecting applications while maintaining goodwill")
                        .isActive(true)
                        .build(),

                // Custom Template
                EmailTemplate.builder()
                        .name("Custom Email")
                        .type(EmailTemplateType.CUSTOM)
                        .subject("Regarding Your Application at {{company_name}}")
                        .content("""
                                <html>
                                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                                    <p>Dear {{candidate_name}},</p>
                                    
                                    <p>[Your message here]</p>
                                    
                                    <p>Best regards,<br/>
                                    <strong>{{employer_name}}</strong><br/>
                                    {{company_name}}<br/>
                                    {{employer_email}}</p>
                                </body>
                                </html>
                                """)
                        .description("Blank template for custom emails")
                        .isActive(true)
                        .build(),

                // Follow-up Interview Template
                EmailTemplate.builder()
                        .name("Second Round Interview")
                        .type(EmailTemplateType.INTERVIEW)
                        .subject("Second Round Interview - {{job_title}} at {{company_name}}")
                        .content("""
                                <html>
                                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                                    <h2 style="color: #0A65CC;">Second Round Interview Invitation</h2>
                                    
                                    <p>Dear {{candidate_name}},</p>
                                    
                                    <p>We were impressed with your first interview and would like to invite you for a second round interview for the <strong>{{job_title}}</strong> position at <strong>{{company_name}}</strong>.</p>
                                    
                                    <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
                                        <h3 style="margin-top: 0; color: #0A65CC;">Interview Details</h3>
                                        <p><strong>Date:</strong> {{interview_date}}</p>
                                        <p><strong>Time:</strong> {{interview_time}}</p>
                                        <p><strong>Location:</strong> {{interview_location}}</p>
                                        <p><strong>Format:</strong> This interview will include technical assessment and meeting with senior team members.</p>
                                    </div>
                                    
                                    <p>Please confirm your availability by replying to this email.</p>
                                    
                                    <p>Looking forward to seeing you!</p>
                                    
                                    <p>Best regards,<br/>
                                    <strong>{{employer_name}}</strong><br/>
                                    {{company_name}}<br/>
                                    {{employer_email}}</p>
                                </body>
                                </html>
                                """)
                        .description("Template for inviting candidates to second round interviews")
                        .isActive(true)
                        .build()
        );

        emailTemplateRepository.saveAll(templates);
    }
}

