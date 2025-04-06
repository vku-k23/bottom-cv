package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.ApplyRequest;
import com.cnpm.bottomcv.dto.response.ApplyResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.ApplyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Apply job API", description = "The API of apply job")
@RestController
@RequestMapping(value = "/api/apply", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class ApplyController {
    private final ApplyService applyService;

    @PostMapping
    public ResponseEntity<ApplyResponse> createApply(@Valid @RequestBody ApplyRequest request) {
        ApplyResponse response = applyService.createApply(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplyResponse> getApplyById(@PathVariable Long id) {
        ApplyResponse response = applyService.getApplyById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ListResponse<ApplyResponse>> getAllApplies(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType
    ) {
        return ResponseEntity.ok(applyService.getAllApplies(pageNo, pageSize, sortBy, sortType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplyResponse> updateApply(@PathVariable Long id, @Valid @RequestBody ApplyRequest request) {
        ApplyResponse response = applyService.updateApply(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApply(@PathVariable Long id) {
        applyService.deleteApply(id);
        return ResponseEntity.noContent().build();
    }
}
