package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.CVRequest;
import com.cnpm.bottomcv.dto.response.CVResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.CVService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CV API", description = "The API of CV")
@RestController
@RequestMapping(value = "/api/cv", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class CVController {
    private final CVService cvService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CVResponse> createCV(@Valid @ModelAttribute CVRequest request) {
        CVResponse response = cvService.createCV(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CVResponse> getCVById(@PathVariable Long id) {
        CVResponse response = cvService.getCVById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ListResponse<CVResponse>> getAllCVs(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType
    ) {
        return ResponseEntity.ok(cvService.getAllCVs(pageNo, pageSize, sortBy, sortType));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CVResponse> updateCV(@PathVariable Long id, @Valid @ModelAttribute CVRequest request) {
        CVResponse response = cvService.updateCV(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCV(@PathVariable Long id) {
        cvService.deleteCV(id);
        return ResponseEntity.noContent().build();
    }
}
