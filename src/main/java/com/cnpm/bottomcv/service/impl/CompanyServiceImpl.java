package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.CompanyRequest;
import com.cnpm.bottomcv.dto.response.CategoryResponse;
import com.cnpm.bottomcv.dto.response.CompanyResponse;
import com.cnpm.bottomcv.dto.response.JobResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.ResourceAlreadyExistException;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Category;
import com.cnpm.bottomcv.model.Company;
import com.cnpm.bottomcv.model.Job;
import com.cnpm.bottomcv.repository.CompanyRepository;
import com.cnpm.bottomcv.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public CompanyResponse createCompany(CompanyRequest request) {
        if (companyRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistException("Slug already exists");
        }

        Company company = new Company();
        mapRequestToEntity(company, request);

        company.setCreatedAt(LocalDateTime.now());
        company.setCreatedBy("system");

        companyRepository.save(company);
        return mapToResponse(company);
    }

    @Override
    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company id", "companyId", id.toString()));
        return mapToResponse(company);
    }

    @Override
    public ListResponse<CompanyResponse> getAllCompanies(int pageNo, int pageSize, String sortBy, String sortType) {
        Sort sortObj = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<Company> pageCompany = companyRepository.findAll(pageable);
        List<Company> companyContent = pageCompany.getContent();

        return ListResponse.<CompanyResponse>builder()
                .data(mapToCompanyListResponse(companyContent))
                .pageNo(pageCompany.getNumber())
                .pageSize(pageCompany.getSize())
                .totalElements((int) pageCompany.getTotalElements())
                .totalPages(pageCompany.getTotalPages())
                .isLast(pageCompany.isLast())
                .build();
    }

    @Override
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company id", "companyId", id.toString()));

        if (!company.getSlug().equals(request.getSlug()) && companyRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistException("Slug already exists");
        }

        mapRequestToEntity(company, request);

        company.setUpdatedAt(LocalDateTime.now());
        company.setUpdatedBy("system");

        companyRepository.save(company);
        return mapToResponse(company);
    }

    @Override
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company id", "companyId", id.toString()));
        companyRepository.delete(company);
    }

    private List<CompanyResponse> mapToCompanyListResponse(List<Company> companyContent) {
        return companyContent.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void mapRequestToEntity(Company company, CompanyRequest request) {
        company.setName(request.getName());
        company.setSlug(request.getSlug());
        company.setIntroduce(request.getIntroduce());
        company.setSocialMediaLinks(request.getSocialMediaLinks());
        company.setAddresses(request.getAddresses());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        company.setWebsite(request.getWebsite());
        company.setLogo(request.getLogo());
        company.setCover(request.getCover());
        company.setIndustry(request.getIndustry());
        company.setCompanySize(request.getCompanySize());
        company.setFoundedYear(request.getFoundedYear());
    }

    private CompanyResponse mapToResponse(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setSlug(company.getSlug());
        response.setIntroduce(company.getIntroduce());
        response.setSocialMediaLinks(company.getSocialMediaLinks());
        response.setAddresses(company.getAddresses());
        response.setPhone(company.getPhone());
        response.setEmail(company.getEmail());
        response.setWebsite(company.getWebsite());
        response.setLogo(company.getLogo());
        response.setCover(company.getCover());
        response.setIndustry(company.getIndustry());
        response.setCompanySize(company.getCompanySize());
        response.setFoundedYear(company.getFoundedYear());

        response.setJobs(company.getJobs().stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList()));

        return response;
    }

    private JobResponse mapToJobResponse(Job job) {
        JobResponse jobResponse = new JobResponse();
        jobResponse.setId(job.getId());
        jobResponse.setTitle(job.getTitle());
        jobResponse.setJobDescription(job.getJobDescription());
        jobResponse.setJobRequirement(job.getJobRequirement());
        jobResponse.setJobBenefit(job.getJobBenefit());
        jobResponse.setJobType(job.getJobType());
        jobResponse.setLocation(job.getLocation());
        jobResponse.setWorkTime(job.getWorkTime());
        jobResponse.setSalary(job.getSalary());
        jobResponse.setExpiryDate(job.getExpiryDate());
        jobResponse.setStatus(job.getStatus());
        jobResponse.setCreatedAt(job.getCreatedAt());
        jobResponse.setCreatedBy(job.getCreatedBy());
        jobResponse.setUpdatedAt(job.getUpdatedAt());
        jobResponse.setUpdatedBy(job.getUpdatedBy());

        CompanyResponse companyResponse = new CompanyResponse();
        companyResponse.setId(job.getCompany().getId());
        companyResponse.setName(job.getCompany().getName());
        companyResponse.setSlug(job.getCompany().getSlug());
        companyResponse.setIntroduce(job.getCompany().getIntroduce());
        companyResponse.setSocialMediaLinks(job.getCompany().getSocialMediaLinks());
        companyResponse.setAddresses(job.getCompany().getAddresses());
        companyResponse.setPhone(job.getCompany().getPhone());
        companyResponse.setEmail(job.getCompany().getEmail());
        companyResponse.setWebsite(job.getCompany().getWebsite());
        companyResponse.setLogo(job.getCompany().getLogo());
        companyResponse.setCover(job.getCompany().getCover());
        companyResponse.setIndustry(job.getCompany().getIndustry());
        companyResponse.setCompanySize(job.getCompany().getCompanySize());
        companyResponse.setFoundedYear(job.getCompany().getFoundedYear());
        jobResponse.setCompany(companyResponse);

        jobResponse.setCategories(job.getCategories().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toSet()));

        return jobResponse;
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