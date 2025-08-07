package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private boolean success;
    private String message;
    private String objectName;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String contentType;
}
