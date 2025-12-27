package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserStatusRequest {
    @NotNull(message = "Status cannot be null")
    private UserStatus status;
    private String reason; // reason for ban/deactivation
}

