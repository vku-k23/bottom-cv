package com.cnpm.bottomcv.config;

import com.cnpm.bottomcv.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioInitializer implements ApplicationRunner {

    private final MinioService minioService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Initializing MinIO storage...");
            minioService.createBucketIfNotExists();
            log.info("MinIO storage initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize MinIO storage: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
            // You might want to implement retry logic here
        }
    }
}
