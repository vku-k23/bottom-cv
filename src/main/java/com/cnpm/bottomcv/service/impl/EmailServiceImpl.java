package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@bottomcv.com}")
    private String fromEmail;

    @Value("${bottom-cv.mail.from-name:BottomCV}")
    private String fromName;

    @Value("${bottom-cv.client:http://localhost:3000}")
    private String clientUrl;

    @Override
    public void sendVerificationEmail(String email, String token) {
        String subject = "Verify Your Email - BottomCV";
        String verificationLink = clientUrl + "/verify-email?token=" + token;

        String content = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #0A65CC;">Welcome to BottomCV!</h2>
                        <p>Thank you for registering. Please verify your email address by clicking the button below:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #0A65CC; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                                Verify Email
                            </a>
                        </div>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #0A65CC;">%s</p>
                        <p style="color: #666; font-size: 14px;">This link will expire in 15 minutes.</p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">If you didn't create an account, you can safely ignore this email.</p>
                    </div>
                </body>
                </html>
                """
                .formatted(verificationLink, verificationLink);

        sendEmail(email, subject, content);
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        String subject = "Reset Your Password - BottomCV";
        String resetLink = clientUrl + "/reset-password?token=" + token;

        String content = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #0A65CC;">Password Reset Request</h2>
                        <p>We received a request to reset your password. Click the button below to create a new password:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #0A65CC; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                                Reset Password
                            </a>
                        </div>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #0A65CC;">%s</p>
                        <p style="color: #666; font-size: 14px;">This link will expire in 15 minutes.</p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="color: #999; font-size: 12px;">If you didn't request a password reset, you can safely ignore this email.</p>
                    </div>
                </body>
                </html>
                """
                .formatted(resetLink, resetLink);

        sendEmail(email, subject, content);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
