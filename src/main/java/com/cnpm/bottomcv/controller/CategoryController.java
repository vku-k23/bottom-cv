package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.CategoryRequest;
import com.cnpm.bottomcv.dto.response.CategoryResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Category API", description = "The API of category")
@RestController
@RequestMapping(value = "/api/v1", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // Back APIs (for dashboard - ADMIN)
    @PostMapping("/back/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.create(request));
    }

    @PutMapping("/back/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/back/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/back/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> getCategoryByIdForBack(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping("/back/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListResponse<CategoryResponse>> getAllCategoriesForBack(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType) {
        return ResponseEntity.ok(categoryService.getAll(pageNo, pageSize, sortBy, sortType));
    }

    // Front APIs (for client web - public)
    @GetMapping("/front/categories/{id}")
    public ResponseEntity<CategoryResponse> getCategoryByIdForFront(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping("/front/categories")
    public ResponseEntity<ListResponse<CategoryResponse>> getAllCategoriesForFront(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortType) {
        return ResponseEntity.ok(categoryService.getAll(pageNo, pageSize, sortBy, sortType));
    }

}