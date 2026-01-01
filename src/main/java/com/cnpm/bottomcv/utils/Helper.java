package com.cnpm.bottomcv.utils;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.exception.UnauthorizedException;
import com.cnpm.bottomcv.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

import java.util.Arrays;

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

    /**
     * Check if user has any of the specified roles
     */
    public static boolean hasRole(User user, RoleType... roles) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> Arrays.asList(roles).contains(role.getName()));
    }

    /**
     * Check if user has a specific role, throws UnauthorizedException if not
     */
    public static void checkRole(User user, RoleType... roles) {
        if (!hasRole(user, roles)) {
            throw new UnauthorizedException("You don't have permission to perform this action. Required roles: " + Arrays.toString(roles));
        }
    }
}
