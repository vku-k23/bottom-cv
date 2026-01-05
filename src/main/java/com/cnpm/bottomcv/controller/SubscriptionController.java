package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.dto.response.SubscriptionResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.exception.UnauthorizedException;
import com.cnpm.bottomcv.model.Subscription;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.service.SubscriptionService;
import com.cnpm.bottomcv.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Subscriptions API", description = "API endpoints for subscription management")
@RestController
@RequestMapping(value = "/api/v1/back/subscriptions", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    @Operation(summary = "Get company's active subscription", description = "Returns the active subscription for a company. EMPLOYER can only access their own company's subscription.")
    public ResponseEntity<SubscriptionResponse> getCompanySubscription(
            @PathVariable Long companyId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        RoleType currentRole = Helper.getCurrentRole(authentication);

        // EMPLOYER can only access their own company's subscription
        if (currentRole == RoleType.EMPLOYER) {
            if (currentUser.getCompany() == null || !currentUser.getCompany().getId().equals(companyId)) {
                throw new UnauthorizedException("You can only access your own company's subscription");
            }
        }

        Optional<Subscription> subscription = subscriptionService.getActiveSubscriptionByCompanyId(companyId);
        
        if (subscription.isEmpty()) {
            throw new ResourceNotFoundException("Subscription", "companyId", companyId.toString());
        }

        SubscriptionResponse response = mapToResponse(subscription.get());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify/{companyId}")
    @PreAuthorize("hasAnyRole('EMPLOYER','ADMIN')")
    @Operation(summary = "Verify subscription exists and is active", description = "Returns true if company has an active subscription, false otherwise. EMPLOYER can only verify their own company's subscription.")
    public ResponseEntity<SubscriptionVerificationResponse> verifySubscription(
            @PathVariable Long companyId,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        RoleType currentRole = Helper.getCurrentRole(authentication);

        // EMPLOYER can only verify their own company's subscription
        if (currentRole == RoleType.EMPLOYER) {
            if (currentUser.getCompany() == null || !currentUser.getCompany().getId().equals(companyId)) {
                throw new UnauthorizedException("You can only verify your own company's subscription");
            }
        }

        boolean hasActiveSubscription = subscriptionService.verifySubscriptionExists(companyId);
        
        return ResponseEntity.ok(new SubscriptionVerificationResponse(hasActiveSubscription));
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .companyId(subscription.getCompany().getId())
                .planType(subscription.getPlanType())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .expiryDate(subscription.getExpiryDate())
                .build();
    }

    // Inner class for verification response
    private static class SubscriptionVerificationResponse {
        private final boolean hasActiveSubscription;

        public SubscriptionVerificationResponse(boolean hasActiveSubscription) {
            this.hasActiveSubscription = hasActiveSubscription;
        }

        public boolean isHasActiveSubscription() {
            return hasActiveSubscription;
        }
    }
}

