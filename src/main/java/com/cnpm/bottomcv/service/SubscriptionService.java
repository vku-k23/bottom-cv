package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.constant.SubscriptionPlanType;
import com.cnpm.bottomcv.constant.SubscriptionStatus;
import com.cnpm.bottomcv.model.Subscription;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SubscriptionService {
    Subscription createSubscription(Long companyId, SubscriptionPlanType planType, LocalDateTime startDate, LocalDateTime expiryDate, String stripeSessionId);
    
    Optional<Subscription> getActiveSubscriptionByCompanyId(Long companyId);
    
    boolean verifySubscriptionExists(Long companyId);
    
    void updateSubscriptionStatus(Long subscriptionId, SubscriptionStatus status);
    
    Subscription updateSubscriptionFromStripe(String stripeSessionId, String stripePaymentIntentId, String stripeCustomerId);
    
    int getJobLimitForPlan(SubscriptionPlanType planType);
    
    int getSavedCandidatesLimitForPlan(SubscriptionPlanType planType);
    
    int getResumeVisibilityDaysForPlan(SubscriptionPlanType planType);
}

