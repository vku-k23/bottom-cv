package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.SystemConfigRequest;
import com.cnpm.bottomcv.dto.response.SystemConfigResponse;
import com.cnpm.bottomcv.service.impl.SystemConfigServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "System Config API", description = "API for system configuration management")
@RestController
@RequestMapping(value = "/api/v1/back/system", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigServiceImpl systemConfigService;

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemConfigResponse> getSystemConfig() {
        SystemConfigResponse config = systemConfigService.getSystemConfig();
        return ResponseEntity.ok(config);
    }

    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemConfigResponse> updateSystemConfig(
            @Valid @RequestBody SystemConfigRequest request) {
        SystemConfigResponse config = systemConfigService.updateSystemConfig(request);
        return ResponseEntity.ok(config);
    }
}

