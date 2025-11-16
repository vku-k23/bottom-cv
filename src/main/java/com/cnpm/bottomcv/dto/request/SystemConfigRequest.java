package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfigRequest {
    @NotBlank(message = "Site name is required")
    @Size(max = 100, message = "Site name must not exceed 100 characters")
    private String siteName;
    
    @Size(max = 500, message = "Site description must not exceed 500 characters")
    private String siteDescription;
    
    @Email(message = "Invalid contact email")
    private String contactEmail;
    
    private String contactPhone;
    
    private String logoUrl;
    
    private String faviconUrl;
    
    // Email settings
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    
    // Feature flags
    private Map<String, Boolean> featureFlags;
    
    // Security settings
    private Integer passwordMinLength;
    private Integer sessionTimeoutMinutes;
    private Integer maxLoginAttempts;
    
    // Maintenance mode
    private Boolean maintenanceMode;
    private String maintenanceMessage;
}

