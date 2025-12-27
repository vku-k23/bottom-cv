package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.UpdateUserRolesRequest;
import com.cnpm.bottomcv.dto.request.UpdateUserStatusRequest;
import com.cnpm.bottomcv.dto.response.UserResponse;
import com.cnpm.bottomcv.service.AdminUserManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Users Management API", description = "API for admin user management operations")
@RestController
@RequestMapping(value = "/api/v1/back/users", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        UserResponse response = adminUserManagementService.updateRoles(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> activate(@PathVariable Long id) {
        UserResponse response = adminUserManagementService.activate(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserResponse response = adminUserManagementService.deactivate(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/impersonate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> impersonate(@PathVariable Long id) {
        adminUserManagementService.impersonate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminResetPassword(@PathVariable Long id) {
        adminUserManagementService.adminResetPassword(id);
        return ResponseEntity.ok().build();
    }
}