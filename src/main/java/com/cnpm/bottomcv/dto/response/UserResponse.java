package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import com.cnpm.bottomcv.constant.UserStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String userCode;
    private String username;
    private Set<RoleResponse> roles;
    private ProfileResponse profile;
    private UserStatus status;
    private String createdAt;
    private String updatedAt;
}
