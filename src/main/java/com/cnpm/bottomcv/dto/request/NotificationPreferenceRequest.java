package com.cnpm.bottomcv.dto.request;

import lombok.Data;

@Data
public class NotificationPreferenceRequest {
    private boolean emailEnabled;
    private boolean pushEnabled;
}