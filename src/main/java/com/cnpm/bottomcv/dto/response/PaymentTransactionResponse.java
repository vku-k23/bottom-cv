package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionResponse {
    private Long id;
    private String transactionId;
    private Long userId;
    private String userName;
    private String userEmail;
    private Double amount;
    private String currency;
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED
    private String paymentMethod;
    private String description;
    private String createdAt;
    private String completedAt;
    private String refundedAt;
    private String refundReason;
}

