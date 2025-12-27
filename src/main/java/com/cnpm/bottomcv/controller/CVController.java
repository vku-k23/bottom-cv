package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.CVRequest;
import com.cnpm.bottomcv.dto.response.CVResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.CVService;
import com.cnpm.bottomcv.model.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CV API", description = "The API of CV")
@RestController
@RequestMapping(value = "/api/v1", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class CVController {
    private final CVService cvService;

    // Back APIs (for dashboard - EMPLOYER, ADMIN)
    @GetMapping("/back/cvs/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<CVResponse> getCVByIdForBack(@PathVariable Long id) {
        CVResponse response = cvService.getCVById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/back/cvs")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ListResponse<CVResponse>> getAllCVsForBack(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType) {
        return ResponseEntity.ok(cvService.getAllCVs(pageNo, pageSize, sortBy, sortType));
    }

    // Front APIs (for client web - CANDIDATE)
    @PostMapping(value = "/front/cvs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CVResponse> createCV(@Valid @ModelAttribute CVRequest request) {
        CVResponse response = cvService.createCV(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/front/cvs/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CVResponse> getCVByIdForFront(@PathVariable Long id) {
        CVResponse response = cvService.getCVById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/front/cvs")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ListResponse<CVResponse>> getAllCVsForFront(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(cvService.getAllMyCVs(user.getId(), pageNo, pageSize, sortBy, sortType));
    }

    @PutMapping(value = "/front/cvs/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CVResponse> updateCV(@PathVariable Long id, @Valid @ModelAttribute CVRequest request) {
        CVResponse response = cvService.updateCV(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/front/cvs/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> deleteCV(@PathVariable Long id) {
        cvService.deleteCV(id);
        return ResponseEntity.noContent().build();
    }
}
