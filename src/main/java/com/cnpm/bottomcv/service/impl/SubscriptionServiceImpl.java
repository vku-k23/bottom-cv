package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.SubscriptionPlanType;
import com.cnpm.bottomcv.constant.SubscriptionStatus;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Company;
import com.cnpm.bottomcv.model.Subscription;
import com.cnpm.bottomcv.repository.CompanyRepository;
import com.cnpm.bottomcv.repository.SubscriptionRepository;
import com.cnpm.bottomcv.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public Subscription createSubscription(Long companyId, SubscriptionPlanType planType, 
                                          LocalDateTime startDate, LocalDateTime expiryDate, 
                                          String stripeSessionId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId.toString()));

        Subscription subscription = Subscription.builder()
                .company(company)
                .planType(planType)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(startDate)
                .expiryDate(expiryDate)
                .stripeSessionId(stripeSessionId)
                .build();

        return subscriptionRepository.save(subscription);
    }

    @Override
    public Optional<Subscription> getActiveSubscriptionByCompanyId(Long companyId) {
        List<Subscription> activeSubscriptions = subscriptionRepository.findActiveByCompanyIdList(companyId, LocalDateTime.now());
        // Return the most recent subscription (first in the sorted list)
        return activeSubscriptions.isEmpty() ? Optional.empty() : Optional.of(activeSubscriptions.get(0));
    }

    @Override
    public boolean verifySubscriptionExists(Long companyId) {
        Optional<Subscription> subscription = getActiveSubscriptionByCompanyId(companyId);
        return subscription.isPresent();
    }

    @Override
    @Transactional
    public void updateSubscriptionStatus(Long subscriptionId, SubscriptionStatus status) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId.toString()));
        
        subscription.setStatus(status);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription.setUpdatedBy("system");
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public Subscription updateSubscriptionFromStripe(String stripeSessionId, String stripePaymentIntentId, String stripeCustomerId) {
        Subscription subscription = subscriptionRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "stripeSessionId", stripeSessionId));

        subscription.setStripePaymentIntentId(stripePaymentIntentId);
        subscription.setStripeCustomerId(stripeCustomerId);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription.setUpdatedBy("system");

        return subscriptionRepository.save(subscription);
    }

    @Override
    public int getJobLimitForPlan(SubscriptionPlanType planType) {
        return switch (planType) {
            case BASIC -> 1;
            case STANDARD -> 3;
            case PREMIUM -> 6;
        };
    }

    @Override
    public int getSavedCandidatesLimitForPlan(SubscriptionPlanType planType) {
        return switch (planType) {
            case BASIC -> 5;
            case STANDARD -> 10;
            case PREMIUM -> 20;
        };
    }

    @Override
    public int getResumeVisibilityDaysForPlan(SubscriptionPlanType planType) {
        return switch (planType) {
            case BASIC -> 10;
            case STANDARD -> 20;
            case PREMIUM -> 30;
        };
    }
}

