package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.RoleType;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRolesRequest {
    @NotEmpty(message = "Roles cannot be empty")
    private Set<RoleType> roles;
}

