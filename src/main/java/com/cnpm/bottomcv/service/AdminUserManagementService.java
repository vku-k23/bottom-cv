package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.UpdateUserRolesRequest;
import com.cnpm.bottomcv.dto.request.UpdateUserStatusRequest;
import com.cnpm.bottomcv.dto.response.UserResponse;

public interface AdminUserManagementService {
    UserResponse updateRoles(Long userId, UpdateUserRolesRequest request);

    UserResponse activate(Long userId);

    UserResponse deactivate(Long userId, UpdateUserStatusRequest request);

    void impersonate(Long userId);

    void adminResetPassword(Long userId);
}