package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.CompanyRequest;
import com.cnpm.bottomcv.dto.response.CompanyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import org.springframework.security.core.Authentication;

public interface CompanyService {
    CompanyResponse createCompany(CompanyRequest request, Authentication authentication);

    CompanyResponse updateCompany(Long id, CompanyRequest request, Authentication authentication);

    void deleteCompany(Long id, Authentication authentication);

    ListResponse<CompanyResponse> getAllCompanies(int pageNo, int pageSize, String sortBy, String sortType,
            Authentication authentication);

    CompanyResponse getCompanyById(Long id, Authentication authentication);
}
