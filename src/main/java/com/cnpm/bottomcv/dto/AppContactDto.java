package com.cnpm.bottomcv.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "bottom-cv")
@Getter
@Setter
public class AppContactDto {
    private String message;
    private Map<String, String> contactInfo;
    private List<String> onCallSupport;
}
