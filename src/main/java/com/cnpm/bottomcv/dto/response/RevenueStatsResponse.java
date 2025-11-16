package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueStatsResponse {
    private Double totalRevenue;
    private Double todayRevenue;
    private Double weekRevenue;
    private Double monthRevenue;
    private Double yearRevenue;
    
    private Long totalTransactions;
    private Long completedTransactions;
    private Long pendingTransactions;
    private Long failedTransactions;
    private Long refundedTransactions;
    
    private Double averageTransactionValue;
    private Double growthRate; // percentage
}

