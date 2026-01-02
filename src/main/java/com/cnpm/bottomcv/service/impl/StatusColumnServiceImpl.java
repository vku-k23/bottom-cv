package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.AppConstant;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.dto.request.CreateStatusColumnRequest;
import com.cnpm.bottomcv.dto.request.UpdateStatusColumnRequest;
import com.cnpm.bottomcv.dto.response.StatusColumnResponse;
import com.cnpm.bottomcv.exception.BadRequestException;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.exception.UnauthorizedException;
import com.cnpm.bottomcv.model.Job;
import com.cnpm.bottomcv.model.StatusColumn;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.JobRepository;
import com.cnpm.bottomcv.repository.StatusColumnRepository;
import com.cnpm.bottomcv.service.StatusColumnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StatusColumnServiceImpl implements StatusColumnService {

    private final StatusColumnRepository statusColumnRepository;
    private final JobRepository jobRepository;

    // Constructor injection
    public StatusColumnServiceImpl(
            StatusColumnRepository statusColumnRepository,
            JobRepository jobRepository) {
        this.statusColumnRepository = statusColumnRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public List<StatusColumnResponse> getAllStatusColumns(Long jobId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Check if user has ADMIN or EMPLOYER role
        boolean hasAdminRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ADMIN);
        boolean hasEmployerRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.EMPLOYER);

        if (!hasAdminRole && !hasEmployerRole) {
            throw new UnauthorizedException("Only ADMIN and EMPLOYER can view status columns.");
        }

        // For EMPLOYER (without ADMIN role), verify they own the job
        if (hasEmployerRole && !hasAdminRole && jobId != null) {
            if (currentUser.getCompany() == null) {
                throw new ResourceNotFoundException("Company", "user", currentUser.getId().toString());
            }
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId.toString()));
            if (!job.getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new UnauthorizedException("You can only view status columns for your company's jobs.");
            }
        }

        // Always get global columns (default columns) first
        List<StatusColumn> globalColumns = statusColumnRepository.findAllGlobalColumnsOrderByDisplayOrderAsc();

        // Get job-specific columns if jobId is provided
        List<StatusColumn> jobColumns = new ArrayList<>();
        if (jobId != null) {
            jobColumns = statusColumnRepository.findByJobIdOrderByDisplayOrderAsc(jobId);
        }

        // Combine: global columns (default) + job-specific columns
        List<StatusColumn> allColumns = new ArrayList<>(globalColumns);
        allColumns.addAll(jobColumns);

        // If no default columns found, log warning (they should be created by
        // migration)
        if (globalColumns.isEmpty() || globalColumns.stream().noneMatch(StatusColumn::getIsDefault)) {
            log.warn("Default status columns not found. " +
                    "Please ensure database migration has been executed. " +
                    "Default columns (PENDING, ACTIVE, INACTIVE) should exist in the database.");
        }

        return allColumns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StatusColumnResponse createStatusColumn(CreateStatusColumnRequest request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        verifyStatusColumnAccess(currentUser);

        validateStatusColumnNameUniqueness(request.getName(), request.getJobId());

        Job job = getJobIfSpecified(request.getJobId(), currentUser);
        String code = generateUniqueCode(request.getName(), request.getJobId());
        int maxOrder = calculateMaxDisplayOrder(request.getJobId());

        StatusColumn column = createNewStatusColumn(request, job, code, maxOrder, authentication);
        StatusColumn saved = statusColumnRepository.save(column);
        return mapToResponse(saved);
    }

    private Job getJobIfSpecified(Long jobId, User currentUser) {
        if (jobId == null) {
            return null;
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId.toString()));

        boolean hasAdminRole = hasAdminRole(currentUser);
        boolean hasEmployerRole = hasEmployerRole(currentUser);
        if (hasEmployerRole && !hasAdminRole) {
            verifyEmployerJobOwnership(currentUser, job);
        }
        return job;
    }

    private StatusColumn createNewStatusColumn(CreateStatusColumnRequest request, Job job, String code,
            int maxOrder, Authentication authentication) {
        StatusColumn column = new StatusColumn();
        column.setName(request.getName());
        column.setCode(code);
        column.setDisplayOrder(maxOrder + 1);
        column.setIsDefault(false);
        column.setJob(job);
        column.setCreatedAt(LocalDateTime.now());
        column.setCreatedBy(authentication.getName());
        return column;
    }

    @Override
    @Transactional
    public StatusColumnResponse updateStatusColumn(Long id, UpdateStatusColumnRequest request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        verifyStatusColumnAccess(currentUser);

        StatusColumn column = statusColumnRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(AppConstant.ENTITY_STATUS_COLUMN, "id", id.toString()));

        if (column.getIsDefault()) {
            throw new BadRequestException("Cannot update default system columns.");
        }

        verifyEmployerStatusColumnAccess(currentUser, column);
        updateStatusColumnNameIfChanged(column, request);

        if (request.getDisplayOrder() != null) {
            column.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getDisplayOrder() != null) {
            column.setDisplayOrder(request.getDisplayOrder());
        }

        column.setUpdatedAt(LocalDateTime.now());
        column.setUpdatedBy(authentication.getName());

        StatusColumn saved = statusColumnRepository.save(column);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteStatusColumn(Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        verifyStatusColumnAccess(currentUser);

        StatusColumn column = statusColumnRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(AppConstant.ENTITY_STATUS_COLUMN, "id", id.toString()));

        if (column.getIsDefault()) {
            throw new BadRequestException("Cannot delete default system columns.");
        }

        verifyEmployerStatusColumnAccess(currentUser, column);
        statusColumnRepository.delete(column);
    }

    @Override
    public StatusColumnResponse getStatusColumnById(Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Check if user has ADMIN or EMPLOYER role
        boolean hasAdminRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ADMIN);
        boolean hasEmployerRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.EMPLOYER);

        if (!hasAdminRole && !hasEmployerRole) {
            throw new UnauthorizedException("Only ADMIN and EMPLOYER can view status columns.");
        }

        StatusColumn column = statusColumnRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(AppConstant.ENTITY_STATUS_COLUMN, "id", id.toString()));

        // For EMPLOYER (without ADMIN role), verify they own the job
        if (hasEmployerRole && !hasAdminRole && column.getJob() != null) {
            if (currentUser.getCompany() == null) {
                throw new ResourceNotFoundException("Company", "user", currentUser.getId().toString());
            }
            if (!column.getJob().getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new UnauthorizedException("You can only view status columns for your company's jobs.");
            }
        }

        return mapToResponse(column);
    }

    private StatusColumnResponse mapToResponse(StatusColumn column) {
        return StatusColumnResponse.builder()
                .id(column.getId())
                .name(column.getName())
                .code(column.getCode())
                .displayOrder(column.getDisplayOrder())
                .isDefault(column.getIsDefault())
                .jobId(column.getJob() != null ? column.getJob().getId() : null)
                .createdAt(column.getCreatedAt())
                .updatedAt(column.getUpdatedAt())
                .build();
    }

    private String generateUniqueCode(String name, Long jobId) {
        // Generate code from name (uppercase, replace spaces with underscores)
        String baseCode = name.toUpperCase().replaceAll("[^A-Z0-9]", "_").replaceAll("_+", "_");

        // Check if code exists, if so append UUID
        if (statusColumnRepository.existsByCode(baseCode)) {
            return baseCode + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        return baseCode;
    }

    // Helper methods to reduce cognitive complexity

    /**
     * Check if user has ADMIN role
     */
    private boolean hasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ADMIN);
    }

    /**
     * Check if user has EMPLOYER role
     */
    private boolean hasEmployerRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.EMPLOYER);
    }

    /**
     * Verify user has permission to manage status columns (ADMIN or EMPLOYER)
     */
    private void verifyStatusColumnAccess(User user) {
        boolean hasAdminRole = hasAdminRole(user);
        boolean hasEmployerRole = hasEmployerRole(user);
        if (!hasAdminRole && !hasEmployerRole) {
            throw new UnauthorizedException("Only ADMIN and EMPLOYER can manage status columns.");
        }
    }

    /**
     * Verify employer owns the job for status column operations
     */
    private void verifyEmployerJobOwnership(User employer, Job job) {
        if (employer.getCompany() == null) {
            throw new ResourceNotFoundException("Company", "user", employer.getId().toString());
        }
        if (!job.getCompany().getId().equals(employer.getCompany().getId())) {
            throw new UnauthorizedException("You can only manage status columns for your company's jobs.");
        }
    }

    /**
     * Verify employer can manage status column (if job-specific)
     */
    private void verifyEmployerStatusColumnAccess(User user, StatusColumn column) {
        boolean hasAdminRole = hasAdminRole(user);
        boolean hasEmployerRole = hasEmployerRole(user);
        if (hasEmployerRole && !hasAdminRole && column.getJob() != null) {
            verifyEmployerJobOwnership(user, column.getJob());
        }
    }

    /**
     * Validate name uniqueness for status column
     */
    private void validateStatusColumnNameUniqueness(String name, Long jobId) {
        if (jobId != null) {
            if (statusColumnRepository.existsByNameAndJobId(name, jobId)) {
                throw new BadRequestException("A column with this name already exists for this job.");
            }
        } else {
            if (statusColumnRepository.existsByNameAndJobIdIsNull(name)) {
                throw new BadRequestException("A global column with this name already exists.");
            }
        }
    }

    /**
     * Calculate max display order for status columns
     */
    private int calculateMaxDisplayOrder(Long jobId) {
        return statusColumnRepository.findByJobIdOrGlobalOrderByDisplayOrderAsc(jobId)
                .stream()
                .mapToInt(StatusColumn::getDisplayOrder)
                .max()
                .orElse(-1);
    }

    /**
     * Update status column name if changed and validate uniqueness
     */
    private void updateStatusColumnNameIfChanged(StatusColumn column, UpdateStatusColumnRequest request) {
        if (request.getName() != null && !request.getName().equals(column.getName())) {
            Long jobId = column.getJob() != null ? column.getJob().getId() : null;
            validateStatusColumnNameUniqueness(request.getName(), jobId);
            column.setName(request.getName());
        }
    }

}
