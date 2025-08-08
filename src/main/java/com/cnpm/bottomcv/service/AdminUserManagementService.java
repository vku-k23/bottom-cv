package com.cnpm.bottomcv.service;

public interface AdminUserManagementService {
    void updateRoles(Long userId);

    void activate(Long userId);

    void deactivate(Long userId);

    void impersonate(Long userId);

    void adminResetPassword(Long userId);
}