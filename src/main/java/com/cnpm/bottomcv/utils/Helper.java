package com.cnpm.bottomcv.utils;

import com.cnpm.bottomcv.constant.RoleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

@Slf4j
public class Helper {
    public static RoleType getCurrentRole(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            String role =  authentication.getAuthorities().iterator().next().getAuthority();
            if (role != null) {
                try {
                    String currentRole = role.split("ROLE_")[1];
                    return RoleType.valueOf(currentRole);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid role type: {}", role, e);
                }
            }
        }
        return null;
    }
}
