package com.cnpm.bottomcv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsResponse {
    private Long totalUsers;
    private Long totalCandidates;
    private Long totalEmployers;
    private Long totalCompanies;
    private Long totalJobs;
    private Long activeJobs;
    private Long totalApplications;
    private Long pendingJobs;
    private Long pendingReports;
    private Long totalRevenue;
    
    // Growth metrics
    private Double userGrowthRate; // percentage
    private Double jobGrowthRate;
    private Double applicationGrowthRate;
    
    // Period comparisons
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    private Long newJobsToday;
    private Long newJobsThisWeek;
    private Long newJobsThisMonth;
}

