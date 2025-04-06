package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.CategoryRequest;
import com.cnpm.bottomcv.dto.response.CategoryResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
    CategoryResponse getById(Long id);
    ListResponse<CategoryResponse> getAll(int pageNo, int pageSize, String sortBy, String sortType);
}
