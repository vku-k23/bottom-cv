package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.JobRequest;
import com.cnpm.bottomcv.dto.request.JobSearchRequest;
import com.cnpm.bottomcv.dto.response.*;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Category;
import com.cnpm.bottomcv.model.Company;
import com.cnpm.bottomcv.model.Job;
import com.cnpm.bottomcv.repository.CategoryRepository;
import com.cnpm.bottomcv.repository.CompanyRepository;
import com.cnpm.bottomcv.repository.JobRepository;
import com.cnpm.bottomcv.service.JobService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public JobResponse createJob(JobRequest request) {
        Job job = new Job();
        mapRequestToEntity(job, request);
        job.setCreatedAt(LocalDateTime.now());
        job.setCreatedBy("system");
        jobRepository.save(job);
        return mapToResponse(job);
    }

    @Override
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return mapToResponse(job);
    }

    @Override
    public ListResponse<JobResponse> getAllJobs(JobSearchRequest jobSearchRequest) {
        Specification<Job> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            if (jobSearchRequest.getKeyword() != null && !jobSearchRequest.getKeyword().isEmpty()) {
                String keyword = "%" + jobSearchRequest.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("jobDescription")), keyword)
                ));
            }

            if (jobSearchRequest.getLocation() != null && !jobSearchRequest.getLocation().isEmpty()) {
                predicates.add(cb.equal(root.get("location"), jobSearchRequest.getLocation()));
            }

            if (jobSearchRequest.getJobType() != null) {
                predicates.add(cb.equal(root.get("jobType"), jobSearchRequest.getJobType()));
            }

            if (jobSearchRequest.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), jobSearchRequest.getMinSalary()));
            }

            if (jobSearchRequest.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), jobSearchRequest.getMaxSalary()));
            }

            if (jobSearchRequest.getCategoryId() != null) {
                predicates.add(cb.isMember(
                        categoryRepository.findById(jobSearchRequest.getCategoryId())
                                .orElseThrow(() -> new RuntimeException("Category not found")),
                        root.get("categories")
                ));
            }

            if (jobSearchRequest.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), jobSearchRequest.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.unsorted();
        if (jobSearchRequest.getSortBy() != null && jobSearchRequest.getSortDirection() != null) {
            sort = Sort.by(Sort.Direction.fromString(jobSearchRequest.getSortDirection()), jobSearchRequest.getSortBy());
        }

        Pageable pageable = PageRequest.of(jobSearchRequest.getPage(), jobSearchRequest.getSize(), sort);

        Page<Job> pageJob = jobRepository.findAll(spec, pageable);
        List<Job> jobs = pageJob.getContent();

        return ListResponse.<JobResponse>builder()
                .data(jobs.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNo(pageJob.getNumber())
                .pageSize(pageJob.getSize())
                .totalElements((int) pageJob.getTotalElements())
                .totalPages(pageJob.getTotalPages())
                .isLast(pageJob.isLast())
                .build();
    }

    @Override
    public JobResponse updateJob(Long id, JobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job id", "id", id.toString()));

        mapRequestToEntity(job, request);
        job.setUpdatedAt(LocalDateTime.now());
        job.setUpdatedBy("system");
        jobRepository.save(job);
        return mapToResponse(job);
    }

    @Override
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job id", "id", id.toString()));
        jobRepository.delete(job);
    }

    private void mapRequestToEntity(Job job, JobRequest request) {
        job.setTitle(request.getTitle());
        job.setJobDescription(request.getJobDescription());
        job.setJobRequirement(request.getJobRequirement());
        job.setJobBenefit(request.getJobBenefit());
        job.setJobType(request.getJobType());
        job.setLocation(request.getLocation());
        job.setWorkTime(request.getWorkTime());
        job.setSalary(request.getSalary());
        job.setExpiryDate(request.getExpiryDate());
        job.setStatus(request.getStatus());

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company id", "id", request.getCompanyId().toString()));
        job.setCompany(company);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category id", "id", categoryId.toString()));
                categories.add(category);
            }
            job.setCategories(categories);
        } else {
            job.setCategories(new HashSet<>());
        }
    }

    private JobResponse mapToResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setJobDescription(job.getJobDescription());
        response.setJobRequirement(job.getJobRequirement());
        response.setJobBenefit(job.getJobBenefit());
        response.setJobType(job.getJobType());
        response.setLocation(job.getLocation());
        response.setWorkTime(job.getWorkTime());
        response.setSalary(job.getSalary());
        response.setExpiryDate(job.getExpiryDate());
        response.setStatus(job.getStatus());
        response.setCreatedAt(job.getCreatedAt());
        response.setCreatedBy(job.getCreatedBy());
        response.setUpdatedAt(job.getUpdatedAt());
        response.setUpdatedBy(job.getUpdatedBy());

        response.setCompany(mapToCompanyResponse(job.getCompany()));
        response.setCategories(job.getCategories().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toSet()));

        return response;
    }

    private CompanyResponse mapToCompanyResponse(Company company) {
        CompanyResponse companyResponse = new CompanyResponse();
        companyResponse.setId(company.getId());
        companyResponse.setName(company.getName());
        companyResponse.setSlug(company.getSlug());
        companyResponse.setIntroduce(company.getIntroduce());
        companyResponse.setSocialMediaLinks(company.getSocialMediaLinks());
        companyResponse.setAddresses(company.getAddresses());
        companyResponse.setPhone(company.getPhone());
        companyResponse.setEmail(company.getEmail());
        companyResponse.setWebsite(company.getWebsite());
        companyResponse.setLogo(company.getLogo());
        companyResponse.setCover(company.getCover());
        companyResponse.setIndustry(company.getIndustry());
        companyResponse.setCompanySize(company.getCompanySize());
        companyResponse.setFoundedYear(company.getFoundedYear());
        return companyResponse;
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(category.getId());
        categoryResponse.setName(category.getName());
        categoryResponse.setSlug(category.getSlug());
        categoryResponse.setDescription(category.getDescription());
        return categoryResponse;
    }
}