package com.cnpm.bottomcv.dto.response;

import com.cnpm.bottomcv.constant.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private Long id;
    private RoleType name;
    private String createdAt;
    private String updatedAt;
}
