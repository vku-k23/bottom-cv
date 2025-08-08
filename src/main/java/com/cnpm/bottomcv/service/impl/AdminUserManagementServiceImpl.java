package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.service.AdminUserManagementService;
import org.springframework.stereotype.Service;

@Service
public class AdminUserManagementServiceImpl implements AdminUserManagementService {
    @Override
    public void updateRoles(Long userId) {
        // TODO: implement role update
    }

    @Override
    public void activate(Long userId) {
        // TODO: implement user activation
    }

    @Override
    public void deactivate(Long userId) {
        // TODO: implement user deactivation
    }

    @Override
    public void impersonate(Long userId) {
        // TODO: implement admin impersonation
    }

    @Override
    public void adminResetPassword(Long userId) {
        // TODO: implement admin reset password
    }
}