package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.CompanyRequest;
import com.cnpm.bottomcv.dto.response.CompanyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.CompanyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Company API", description = "The API of company")
@RestController
@RequestMapping(value = "/api/companies", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    // Back APIs (for dashboard - EMPLOYER, ADMIN)
    @PostMapping("/back/companies")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.createCompany(request));
    }

    @PutMapping("/back/companies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable Long id, @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(id, request));
    }

    @DeleteMapping("/back/companies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/back/companies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<CompanyResponse> getCompanyByIdForBack(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @GetMapping("/back/companies")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ListResponse<CompanyResponse>> getAllCompaniesForBack(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType
    ) {
        return ResponseEntity.ok(companyService.getAllCompanies(pageNo, pageSize, sortBy, sortType));
    }

    // Front APIs (for client web - public)
    @GetMapping("/front/companies/{id}")
    public ResponseEntity<CompanyResponse> getCompanyByIdForFront(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @GetMapping("/front/companies")
    public ResponseEntity<ListResponse<CompanyResponse>> getAllCompaniesForFront(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType
    ) {
        return ResponseEntity.ok(companyService.getAllCompanies(pageNo, pageSize, sortBy, sortType));
    }
}
