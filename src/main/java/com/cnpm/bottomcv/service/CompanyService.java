package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.CompanyRequest;
import com.cnpm.bottomcv.dto.response.CompanyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;

public interface CompanyService {
    CompanyResponse createCompany(CompanyRequest request);
    CompanyResponse updateCompany(Long id, CompanyRequest request);
    void deleteCompany(Long id);
    ListResponse<CompanyResponse> getAllCompanies(int pageNo, int pageSize, String sortBy, String sortType);
    CompanyResponse getCompanyById(Long id);
}
