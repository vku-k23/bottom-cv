package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);

    Payment findByReferenceId(String referenceId);
}