package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.constant.StatusJob;
import com.cnpm.bottomcv.dto.response.ActivityLogResponse;
import com.cnpm.bottomcv.dto.response.AdminStatsResponse;
import com.cnpm.bottomcv.dto.response.ChartDataResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException; // Added
import com.cnpm.bottomcv.model.User; // Added
import com.cnpm.bottomcv.repository.*;
import com.cnpm.bottomcv.service.AdminDashboardService;
import com.cnpm.bottomcv.utils.Helper; // Added
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication; // Added
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplyRepository applyRepository;
    private final ReportRepository reportRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public AdminStatsResponse getStats(Authentication authentication) {
        RoleType currentRole = Helper.getCurrentRole(authentication);
        Long currentUserId = ((User) authentication.getPrincipal()).getId();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusMonths(1);
        LocalDateTime startOfLastMonth = now.minusMonths(2);

        if (currentRole == RoleType.ADMIN) {
            log.info("Getting admin dashboard statistics for ADMIN");

            // Total counts
            Long totalUsers = userRepository.count();
            Long totalCandidates = userRepository.countByRoles_Name(RoleType.CANDIDATE);
            Long totalEmployers = userRepository.countByRoles_Name(RoleType.EMPLOYER);
            Long totalCompanies = companyRepository.count();
            Long totalJobs = jobRepository.count();
            Long activeJobs = jobRepository.countByStatus(StatusJob.ACTIVE);
            Long totalApplications = applyRepository.count();
            Long pendingJobs = jobRepository.countByStatus(StatusJob.PENDING);
            Long pendingReports = reportRepository.countByResolved(false);

            // New entities in periods
            Long newUsersToday = userRepository.countByCreatedAtAfter(startOfToday);
            Long newUsersThisWeek = userRepository.countByCreatedAtAfter(startOfWeek);
            Long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

            Long newJobsToday = jobRepository.countByCreatedAtAfter(startOfToday);
            Long newJobsThisWeek = jobRepository.countByCreatedAtAfter(startOfWeek);
            Long newJobsThisMonth = jobRepository.countByCreatedAtAfter(startOfMonth);

            // Calculate growth rates (comparing this month vs last month)
            Long usersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, startOfMonth);
            Double userGrowthRate = calculateGrowthRate(usersLastMonth, newUsersThisMonth);

            Long jobsLastMonth = jobRepository.countByCreatedAtBetween(startOfLastMonth, startOfMonth);
            Double jobGrowthRate = calculateGrowthRate(jobsLastMonth, newJobsThisMonth);

            Long appsLastMonth = applyRepository.countByCreatedAtBetween(startOfLastMonth, startOfMonth);
            Long appsThisMonth = applyRepository.countByCreatedAtAfter(startOfMonth);
            Double applicationGrowthRate = calculateGrowthRate(appsLastMonth, appsThisMonth);

            // Total revenue (if payment system is implemented)
            Long totalRevenue = 0L; // placeholder

            return AdminStatsResponse.builder()
                    .totalUsers(totalUsers)
                    .totalCandidates(totalCandidates)
                    .totalEmployers(totalEmployers)
                    .totalCompanies(totalCompanies)
                    .totalJobs(totalJobs)
                    .activeJobs(activeJobs)
                    .totalApplications(totalApplications)
                    .pendingJobs(pendingJobs)
                    .pendingReports(pendingReports)
                    .totalRevenue(totalRevenue)
                    .userGrowthRate(userGrowthRate)
                    .jobGrowthRate(jobGrowthRate)
                    .applicationGrowthRate(applicationGrowthRate)
                    .newUsersToday(newUsersToday)
                    .newUsersThisWeek(newUsersThisWeek)
                    .newUsersThisMonth(newUsersThisMonth)
                    .newJobsToday(newJobsToday)
                    .newJobsThisWeek(newJobsThisWeek)
                    .newJobsThisMonth(newJobsThisMonth)
                    .build();
        } else if (currentRole == RoleType.EMPLOYER) {
            log.info("Getting employer dashboard statistics for EMPLOYER");

            // Find the employer's company
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId.toString()));
            Long companyId = user.getCompany().getId(); // Assuming employer user has a company

            // Employer-specific stats
            Long totalJobs = jobRepository.countByCompanyId(companyId);
            Long activeJobs = jobRepository.countByCompanyIdAndStatus(companyId, StatusJob.ACTIVE);
            Long pendingJobs = jobRepository.countByCompanyIdAndStatus(companyId, StatusJob.PENDING);
            Long totalApplications = applyRepository.countByJob_CompanyId(companyId);

            Long newJobsThisMonth = jobRepository.countByCompanyIdAndCreatedAtAfter(companyId, startOfMonth);
            Long jobsLastMonth = jobRepository.countByCompanyIdAndCreatedAtBetween(companyId, startOfLastMonth,
                    startOfMonth);
            Double jobGrowthRate = calculateGrowthRate(jobsLastMonth, newJobsThisMonth);

            Long appsThisMonth = applyRepository.countByJob_CompanyIdAndCreatedAtAfter(companyId, startOfMonth);
            Long appsLastMonth = applyRepository.countByJob_CompanyIdAndCreatedAtBetween(companyId, startOfLastMonth,
                    startOfMonth);
            Double applicationGrowthRate = calculateGrowthRate(appsLastMonth, appsThisMonth);

            return AdminStatsResponse.builder()
                    .totalJobs(totalJobs)
                    .activeJobs(activeJobs)
                    .pendingJobs(pendingJobs)
                    .totalApplications(totalApplications)
                    .jobGrowthRate(jobGrowthRate)
                    .applicationGrowthRate(applicationGrowthRate)
                    .newJobsThisMonth(newJobsThisMonth)
                    // Set other fields to 0 or null as they are not relevant for employer
                    .totalUsers(0L)
                    .totalCandidates(0L)
                    .totalEmployers(0L)
                    .totalCompanies(0L)
                    .pendingReports(0L)
                    .totalRevenue(0L)
                    .userGrowthRate(0.0)
                    .newUsersToday(0L)
                    .newUsersThisWeek(0L)
                    .newUsersThisMonth(0L)
                    .newJobsToday(0L)
                    .newJobsThisWeek(0L)
                    .build();
        } else {
            throw new ResourceNotFoundException("Dashboard statistics", "role", currentRole.name());
        }
    }

    @Override
    public List<ActivityLogResponse> getAuditLogs() {
        log.info("Getting recent audit logs");

        // This is a simplified version. In a real system, you would have an AuditLog
        // entity
        // For now, we'll create dummy data from recent entities
        List<ActivityLogResponse> activities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Get recent users
        userRepository.findTop5ByOrderByCreatedAtDesc().stream().limit(5).forEach(user -> {
            activities.add(ActivityLogResponse.builder()
                    .activityType("USER_REGISTRATION")
                    .message("New user registered: " + user.getUsername())
                    .userName(user.getUsername())
                    .userId(user.getId())
                    .status("success")
                    .timestamp(user.getCreatedAt().format(formatter))
                    .resourceType("USER")
                    .resourceId(user.getId())
                    .build());
        });

        // Get recent jobs
        jobRepository.findTop5ByOrderByCreatedAtDesc().stream().limit(5).forEach(job -> {
            activities.add(ActivityLogResponse.builder()
                    .activityType("JOB_POSTED")
                    .message("New job posted: " + job.getTitle())
                    .userName(job.getCompany() != null ? job.getCompany().getName() : "Unknown Company")
                    .status("info")
                    .timestamp(job.getCreatedAt().format(formatter))
                    .resourceType("JOB")
                    .resourceId(job.getId())
                    .build());
        });

        // Get recent applications
        applyRepository.findTop5ByOrderByCreatedAtDesc().stream().limit(5).forEach(apply -> {
            activities.add(ActivityLogResponse.builder()
                    .activityType("APPLICATION_SUBMITTED")
                    .message("Application submitted for: "
                            + (apply.getJob() != null ? apply.getJob().getTitle() : "Unknown Job"))
                    .userName(apply.getUser() != null ? apply.getUser().getUsername() : "Unknown User")
                    .userId(apply.getUser() != null ? apply.getUser().getId() : null)
                    .status("success")
                    .timestamp(apply.getCreatedAt().format(formatter))
                    .resourceType("APPLICATION")
                    .resourceId(apply.getId())
                    .build());
        });

        // Sort by timestamp descending
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return activities.stream().limit(20).toList();
    }

    @Override
    public void getSystemConfig() {
        // Implementation will be done in SystemConfigService
        log.info("getSystemConfig called - to be implemented");
    }

    @Override
    public void updateSystemConfig() {
        // Implementation will be done in SystemConfigService
        log.info("updateSystemConfig called - to be implemented");
    }

    private Double calculateGrowthRate(Long oldValue, Long newValue) {
        if (oldValue == null || oldValue == 0) {
            return newValue > 0 ? 100.0 : 0.0;
        }
        return ((newValue - oldValue) * 100.0) / oldValue;
    }

    public ChartDataResponse getUserGrowthChart(int days) {
        List<ChartDataResponse.DataPoint> dataPoints = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime startOfDay = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            Long count = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);

            dataPoints.add(ChartDataResponse.DataPoint.builder()
                    .label(startOfDay.format(DateTimeFormatter.ofPattern("MM-dd")))
                    .value(count)
                    .build());
        }

        return ChartDataResponse.builder().data(dataPoints).build();
    }

    public ChartDataResponse getJobTrendChart(int days) {
        List<ChartDataResponse.DataPoint> dataPoints = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime startOfDay = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            Long count = jobRepository.countByCreatedAtBetween(startOfDay, endOfDay);

            dataPoints.add(ChartDataResponse.DataPoint.builder()
                    .label(startOfDay.format(DateTimeFormatter.ofPattern("MM-dd")))
                    .value(count)
                    .build());
        }

        return ChartDataResponse.builder().data(dataPoints).build();
    }
}
