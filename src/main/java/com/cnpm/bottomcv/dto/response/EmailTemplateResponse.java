package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.EmailTemplateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailTemplateResponse {
    
    private Long id;
    private String name;
    private EmailTemplateType type;
    private String subject;
    private String content;
    private String description;
    private Boolean isActive;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

