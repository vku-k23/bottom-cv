package com.cnpm.bottomcv.dto.request;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.constant.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFilterRequest {
    private String search; // search by username, email, name
    private RoleType role;
    private UserStatus status;
    private String sortBy; // username, createdAt, etc.
    private String sortDirection; // ASC, DESC
    private Integer page;
    private Integer size;
}

