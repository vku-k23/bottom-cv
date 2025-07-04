package com.cnpm.bottomcv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 255, message = "Message must be less than 255 characters")
    private String message;

    @NotNull(message = "User ID is required")
    private Long userId;
}