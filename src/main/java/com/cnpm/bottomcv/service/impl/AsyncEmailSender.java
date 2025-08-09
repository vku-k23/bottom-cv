package com.cnpm.bottomcv.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncEmailSender {

    private final JavaMailSender mailSender;

    @Async("taskExecutor")
    public void send(MimeMessage mimeMessage) {
        try {
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Async mail send failed", e);
            throw e;
        }
    }
}