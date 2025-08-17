package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.PatternField;
import com.cnpm.bottomcv.service.EmailService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AsyncEmailSender asyncEmailSender;

    @Value("${bottom-cv.client}")
    private String domain;

    @Value("${bottom-cv.brand.name:}")
    private String brandName;

    @Value("${bottom-cv.brand.logo-url:}")
    private String brandLogoUrl;

    @Value("${bottom-cv.brand.support-email:}")
    private String supportEmail;

    @Value("${bottom-cv.mail.from:}")
    private String mailFrom;

    @Value("${bottom-cv.mail.from-name:}")
    private String mailFromName;

    @Value("${bottom-cv.mail.reply-to:}")
    private String mailReplyTo;

    private String buildVerificationUrl(String token) {
        return String.format("%s/auth/verify-email?token=%s", domain, token);
    }

    private String buildResetPasswordUrl(String token) {
        return String.format("%s/auth/confirm-forgot-password?token=%s", domain, token);
    }

    @Override
    public void sendVerificationEmail(String email, String token) {
        if (!Pattern.compile(PatternField.EMAIL_PATTERN).matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        log.info("Sending verification email to {}", email);

        String verificationUrl = buildVerificationUrl(token);
        String subject = String.format("Verify your email for %s", brandName);

        Map<String, Object> variables = new HashMap<>();
        variables.put("preheader", "Confirm your email to finish setting up your account.");
        variables.put("title", "Verify your email");
        variables.put("intro",
                "Thanks for signing up! Please confirm that this is your email address by clicking the button below.");
        variables.put("ctaText", "Verify Email");
        variables.put("actionUrl", verificationUrl);
        variables.put("supportText", String.format(
                "If you didn’t create an account, you can safely ignore this email. If you need help, contact us at %s.",
                supportEmail));
        variables.put("brandName", brandName);
        variables.put("logoUrl", brandLogoUrl);
        variables.put("year", String.valueOf(java.time.Year.now().getValue()));

        sendEmail("verify-email", email, subject, variables);
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        if (!Pattern.compile(PatternField.EMAIL_PATTERN).matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        log.info("Sending password reset email to {}", email);

        String resetUrl = buildResetPasswordUrl(token);
        String subject = String.format("Reset your password for %s", brandName);

        Map<String, Object> variables = new HashMap<>();
        variables.put("preheader", "Reset your password with the link below.");
        variables.put("title", "Reset your password");
        variables.put("intro",
                "We received a request to reset your password. Click the button below to choose a new one.");
        variables.put("ctaText", "Reset Password");
        variables.put("actionUrl", resetUrl);
        variables.put("supportText", String.format(
                "If you didn’t request this, you can safely ignore this email. If you need help, contact us at %s.",
                supportEmail));
        variables.put("brandName", brandName);
        variables.put("logoUrl", brandLogoUrl);
        variables.put("year", String.valueOf(java.time.Year.now().getValue()));

        sendEmail("reset-password", email, subject, variables);
    }

    private void sendEmail(String template, String toEmail, String subject, Map<String, Object> variables) {
        Context context = new Context();
        variables.forEach(context::setVariable);

        String processedHtml = templateEngine.process(template, context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setText(processedHtml, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(mailFrom, mailFromName);

            if (StringUtils.hasText(mailReplyTo)) {
                helper.setReplyTo(mailReplyTo);
            }

            mimeMessage.addHeader("X-Priority", "3");
            mimeMessage.addHeader("X-MSMail-Priority", "Normal");

            asyncEmailSender.send(mimeMessage);
            log.info("Email [{}] dispatched asynchronously to {}", subject, toEmail);
        } catch (Exception e) {
            log.error("Failed to build email [{}] to {}", subject, toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
