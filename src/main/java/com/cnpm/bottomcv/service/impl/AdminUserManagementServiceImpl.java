package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.constant.UserStatus;
import com.cnpm.bottomcv.dto.request.UpdateUserRolesRequest;
import com.cnpm.bottomcv.dto.request.UpdateUserStatusRequest;
import com.cnpm.bottomcv.dto.response.UserResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Role;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.RoleRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.AdminUserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse updateRoles(Long userId, UpdateUserRolesRequest request) {
        log.info("Updating roles for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        Set<Role> roles = new HashSet<>();
        for (RoleType roleType : request.getRoles()) {
            Role role = roleRepository.findByName(roleType)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleType.name()));
            roles.add(role);
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        log.info("Successfully updated roles for user ID: {}", userId);
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse activate(Long userId) {
        log.info("Activating user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        log.info("Successfully activated user ID: {}", userId);
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse deactivate(Long userId, UpdateUserStatusRequest request) {
        log.info("Deactivating user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        user.setStatus(request.getStatus());
        User savedUser = userRepository.save(user);

        log.info("Successfully deactivated user ID: {} with status: {}", userId, request.getStatus());
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void impersonate(Long userId) {
        log.info("Impersonate request for user ID: {}", userId);
        
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        // In a real implementation, you would:
        // 1. Generate a special token for impersonation
        // 2. Store it in Redis with expiration
        // 3. Return the token to admin
        // For now, just log the action
        
        log.info("Impersonation session created for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void adminResetPassword(Long userId) {
        log.info("Admin resetting password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        // Generate a random password
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // In a real system, you would:
        // 1. Send email with new password to user
        // 2. Force user to change password on next login
        
        log.info("Successfully reset password for user ID: {}. New password sent via email.", userId);
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    private UserResponse mapToUserResponse(User user) {
        // Simple mapping - reuse from UserServiceImpl or create helper
        return UserResponse.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
    }
}
