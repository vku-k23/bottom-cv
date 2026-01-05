package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.constant.SubscriptionStatus;
import com.cnpm.bottomcv.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByCompanyId(Long companyId);

    List<Subscription> findByCompanyIdAndStatus(Long companyId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.company.id = :companyId AND s.status = 'ACTIVE' AND s.expiryDate > :now ORDER BY s.startDate DESC, s.createdAt DESC")
    List<Subscription> findActiveByCompanyIdList(@Param("companyId") Long companyId, @Param("now") LocalDateTime now);

    Optional<Subscription> findByStripeSessionId(String stripeSessionId);

    Optional<Subscription> findByStripePaymentIntentId(String stripePaymentIntentId);
}

