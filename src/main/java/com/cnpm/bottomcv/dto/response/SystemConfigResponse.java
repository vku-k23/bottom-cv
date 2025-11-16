package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfigResponse {
    private Long id;
    private String siteName;
    private String siteDescription;
    private String contactEmail;
    private String contactPhone;
    private String logoUrl;
    private String faviconUrl;
    
    // Email settings (masked password)
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    
    // Feature flags
    private Map<String, Boolean> featureFlags;
    
    // Security settings
    private Integer passwordMinLength;
    private Integer sessionTimeoutMinutes;
    private Integer maxLoginAttempts;
    
    // Maintenance mode
    private Boolean maintenanceMode;
    private String maintenanceMessage;
    
    private String updatedAt;
    private String updatedBy;
}

