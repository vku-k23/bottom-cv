package com.cnpm.bottomcv.model;

import com.cnpm.bottomcv.dto.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "system_config")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String siteName;

    @Column(length = 500)
    private String siteDescription;

    @Column(length = 100)
    private String contactEmail;

    @Column(length = 20)
    private String contactPhone;

    private String logoUrl;

    private String faviconUrl;

    // Email settings
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword; // encrypted

    // Feature flags stored as JSON
    @ElementCollection
    @CollectionTable(name = "feature_flags", joinColumns = @JoinColumn(name = "config_id"))
    @MapKeyColumn(name = "feature_name")
    @Column(name = "enabled")
    @Builder.Default
    private Map<String, Boolean> featureFlags = new HashMap<>();

    // Security settings
    @Column(nullable = false)
    @Builder.Default
    private Integer passwordMinLength = 8;

    @Column(nullable = false)
    @Builder.Default
    private Integer sessionTimeoutMinutes = 30;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxLoginAttempts = 5;

    // Maintenance mode
    @Column(nullable = false)
    @Builder.Default
    private Boolean maintenanceMode = false;

    @Column(length = 500)
    private String maintenanceMessage;
}

