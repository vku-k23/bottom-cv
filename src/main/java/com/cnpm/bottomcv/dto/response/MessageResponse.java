package com.cnpm.bottomcv.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private boolean read;
}