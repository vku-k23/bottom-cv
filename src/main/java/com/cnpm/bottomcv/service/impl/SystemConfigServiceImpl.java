package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.SystemConfigRequest;
import com.cnpm.bottomcv.dto.response.SystemConfigResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.SystemConfig;
import com.cnpm.bottomcv.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl {

    private final SystemConfigRepository systemConfigRepository;

    public SystemConfigResponse getSystemConfig() {
        log.info("Getting system configuration");
        
        SystemConfig config = systemConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(this::createDefaultConfig);
        
        return mapToResponse(config);
    }

    @Transactional
    public SystemConfigResponse updateSystemConfig(SystemConfigRequest request) {
        log.info("Updating system configuration");
        
        SystemConfig config = systemConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(SystemConfig::new);

        config.setSiteName(request.getSiteName());
        config.setSiteDescription(request.getSiteDescription());
        config.setContactEmail(request.getContactEmail());
        config.setContactPhone(request.getContactPhone());
        config.setLogoUrl(request.getLogoUrl());
        config.setFaviconUrl(request.getFaviconUrl());
        
        // Email settings
        if (request.getSmtpHost() != null) config.setSmtpHost(request.getSmtpHost());
        if (request.getSmtpPort() != null) config.setSmtpPort(request.getSmtpPort());
        if (request.getSmtpUsername() != null) config.setSmtpUsername(request.getSmtpUsername());
        if (request.getSmtpPassword() != null) {
            // In real system, encrypt the password
            config.setSmtpPassword(request.getSmtpPassword());
        }
        
        // Feature flags
        if (request.getFeatureFlags() != null) {
            config.setFeatureFlags(request.getFeatureFlags());
        }
        
        // Security settings
        if (request.getPasswordMinLength() != null) config.setPasswordMinLength(request.getPasswordMinLength());
        if (request.getSessionTimeoutMinutes() != null) config.setSessionTimeoutMinutes(request.getSessionTimeoutMinutes());
        if (request.getMaxLoginAttempts() != null) config.setMaxLoginAttempts(request.getMaxLoginAttempts());
        
        // Maintenance mode
        if (request.getMaintenanceMode() != null) config.setMaintenanceMode(request.getMaintenanceMode());
        if (request.getMaintenanceMessage() != null) config.setMaintenanceMessage(request.getMaintenanceMessage());

        SystemConfig savedConfig = systemConfigRepository.save(config);
        log.info("Successfully updated system configuration");
        
        return mapToResponse(savedConfig);
    }

    private SystemConfig createDefaultConfig() {
        SystemConfig config = SystemConfig.builder()
                .siteName("BottomCV")
                .siteDescription("Job Portal Platform")
                .passwordMinLength(8)
                .sessionTimeoutMinutes(30)
                .maxLoginAttempts(5)
                .maintenanceMode(false)
                .build();
        return systemConfigRepository.save(config);
    }

    private SystemConfigResponse mapToResponse(SystemConfig config) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return SystemConfigResponse.builder()
                .id(config.getId())
                .siteName(config.getSiteName())
                .siteDescription(config.getSiteDescription())
                .contactEmail(config.getContactEmail())
                .contactPhone(config.getContactPhone())
                .logoUrl(config.getLogoUrl())
                .faviconUrl(config.getFaviconUrl())
                .smtpHost(config.getSmtpHost())
                .smtpPort(config.getSmtpPort())
                .smtpUsername(config.getSmtpUsername())
                // Don't expose password
                .featureFlags(config.getFeatureFlags())
                .passwordMinLength(config.getPasswordMinLength())
                .sessionTimeoutMinutes(config.getSessionTimeoutMinutes())
                .maxLoginAttempts(config.getMaxLoginAttempts())
                .maintenanceMode(config.getMaintenanceMode())
                .maintenanceMessage(config.getMaintenanceMessage())
                .updatedAt(config.getUpdatedAt() != null ? config.getUpdatedAt().format(formatter) : null)
                .updatedBy(config.getUpdatedBy())
                .build();
    }
}

