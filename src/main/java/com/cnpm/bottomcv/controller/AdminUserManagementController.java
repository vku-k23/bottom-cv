package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.service.AdminUserManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Users Management API", description = "Skeleton endpoints for admin user management")
@RestController
@RequestMapping(value = "/api/v1/back/users", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateRoles(@PathVariable Long id) {
        adminUserManagementService.updateRoles(id);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        adminUserManagementService.activate(id);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        adminUserManagementService.deactivate(id);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/impersonate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> impersonate(@PathVariable Long id) {
        adminUserManagementService.impersonate(id);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminResetPassword(@PathVariable Long id) {
        adminUserManagementService.adminResetPassword(id);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}