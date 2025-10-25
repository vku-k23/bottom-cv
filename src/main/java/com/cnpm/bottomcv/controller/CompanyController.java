package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.CompanyRequest;
import com.cnpm.bottomcv.dto.response.CompanyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.CompanyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Company API", description = "The API of company")
@RestController
@RequestMapping(value = "/api/v1", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    // Back APIs (for dashboard - EMPLOYER, ADMIN)
    @PostMapping("/back/companies")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request,
            Authentication authentication) {
        var response = companyService.createCompany(request, authentication);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/back/companies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable Long id,
            @Valid @RequestBody CompanyRequest request, Authentication authentication) {
        return ResponseEntity.ok(companyService.updateCompany(id, request, authentication));
    }

    @DeleteMapping("/back/companies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id, Authentication authentication) {
        companyService.deleteCompany(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/back/companies/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<CompanyResponse> getCompanyByIdForBack(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(companyService.getCompanyById(id, authentication));
    }

    @GetMapping("/back/companies")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ListResponse<CompanyResponse>> getAllCompaniesForBack(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType,
            Authentication authentication) {
        return ResponseEntity.ok(
                companyService.getAllCompanies(pageNo, pageSize, sortBy, sortType, authentication));
    }

    // Front APIs (for client web - public)
    @GetMapping("/front/companies/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER', 'ADMIN')")
    public ResponseEntity<CompanyResponse> getCompanyByIdForFront(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(companyService.getCompanyById(id, authentication));
    }

    @GetMapping("/front/companies")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER', 'ADMIN')")
    public ResponseEntity<ListResponse<CompanyResponse>> getAllCompaniesForFront(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType,
            Authentication authentication) {
        return ResponseEntity
                .ok(companyService.getAllCompanies(pageNo, pageSize, sortBy, sortType, authentication));
    }
}
