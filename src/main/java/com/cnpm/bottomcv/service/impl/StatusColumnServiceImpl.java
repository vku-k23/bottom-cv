package com.cnpm.bottomcv.service.impl;

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
        
        // If no default columns found, log warning (they should be created by migration)
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
        
        // Check if user has ADMIN or EMPLOYER role
        boolean hasAdminRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ADMIN);
        boolean hasEmployerRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.EMPLOYER);
        
        if (!hasAdminRole && !hasEmployerRole) {
            throw new UnauthorizedException("Only ADMIN and EMPLOYER can create status columns.");
        }

        // Validate name uniqueness
        if (request.getJobId() != null) {
            if (statusColumnRepository.existsByNameAndJobId(request.getName(), request.getJobId())) {
                throw new BadRequestException("A column with this name already exists for this job.");
            }
        } else {
            if (statusColumnRepository.existsByNameAndJobIdIsNull(request.getName())) {
                throw new BadRequestException("A global column with this name already exists.");
            }
        }

        // For EMPLOYER (without ADMIN role), verify they own the job
        Job job = null;
        if (request.getJobId() != null) {
            job = jobRepository.findById(request.getJobId())
                    .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId().toString()));
            
            if (hasEmployerRole && !hasAdminRole) {
                if (currentUser.getCompany() == null) {
                    throw new ResourceNotFoundException("Company", "user", currentUser.getId().toString());
                }
                if (!job.getCompany().getId().equals(currentUser.getCompany().getId())) {
                    throw new UnauthorizedException("You can only create status columns for your company's jobs.");
                }
            }
        }

        // Generate unique code
        String code = generateUniqueCode(request.getName(), request.getJobId());

        // Get max display order
        Integer maxOrder = statusColumnRepository.findByJobIdOrGlobalOrderByDisplayOrderAsc(request.getJobId())
                .stream()
                .mapToInt(StatusColumn::getDisplayOrder)
                .max()
                .orElse(-1);

        StatusColumn column = new StatusColumn();
        column.setName(request.getName());
        column.setCode(code);
        column.setDisplayOrder(maxOrder + 1);
        column.setIsDefault(false);
        column.setJob(job);
        column.setCreatedAt(LocalDateTime.now());
        column.setCreatedBy(authentication.getName());

        StatusColumn saved = statusColumnRepository.save(column);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public StatusColumnResponse updateStatusColumn(Long id, UpdateStatusColumnRequest request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Check if user has ADMIN or EMPLOYER role
        boolean hasAdminRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ADMIN);
        boolean hasEmployerRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.EMPLOYER);
        
        if (!hasAdminRole && !hasEmployerRole) {
            throw new UnauthorizedException("Only ADMIN and EMPLOYER can update status columns.");
        }

        StatusColumn column = statusColumnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StatusColumn", "id", id.toString()));

        // Cannot update default columns
        if (column.getIsDefault()) {
            throw new BadRequestException("Cannot update default system columns.");
        }

        // For EMPLOYER (without ADMIN role), verify they own the job
        if (hasEmployerRole && !hasAdminRole && column.getJob() != null) {
            if (currentUser.getCompany() == null) {
                throw new ResourceNotFoundException("Company", "user", currentUser.getId().toString());
            }
            if (!column.getJob().getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new UnauthorizedException("You can only update status columns for your company's jobs.");
            }
        }

        if (request.getName() != null && !request.getName().equals(column.getName())) {
            // Validate name uniqueness
            if (column.getJob() != null) {
                if (statusColumnRepository.existsByNameAndJobId(request.getName(), column.getJob().getId())) {
                    throw new BadRequestException("A column with this name already exists for this job.");
                }
            } else {
                if (statusColumnRepository.existsByNameAndJobIdIsNull(request.getName())) {
                    throw new BadRequestException("A global column with this name already exists.");
                }
            }
            column.setName(request.getName());
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
        
        // Check if user has ADMIN or EMPLOYER role
        boolean hasAdminRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ADMIN);
        boolean hasEmployerRole = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.EMPLOYER);
        
        if (!hasAdminRole && !hasEmployerRole) {
            throw new UnauthorizedException("Only ADMIN and EMPLOYER can delete status columns.");
        }

        StatusColumn column = statusColumnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StatusColumn", "id", id.toString()));

        // Cannot delete default columns
        if (column.getIsDefault()) {
            throw new BadRequestException("Cannot delete default system columns.");
        }

        // For EMPLOYER (without ADMIN role), verify they own the job
        if (hasEmployerRole && !hasAdminRole && column.getJob() != null) {
            if (currentUser.getCompany() == null) {
                throw new ResourceNotFoundException("Company", "user", currentUser.getId().toString());
            }
            if (!column.getJob().getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new UnauthorizedException("You can only delete status columns for your company's jobs.");
            }
        }

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
                .orElseThrow(() -> new ResourceNotFoundException("StatusColumn", "id", id.toString()));

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

}

