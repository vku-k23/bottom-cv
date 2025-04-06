package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.CategoryRequest;
import com.cnpm.bottomcv.dto.response.CategoryResponse;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.exception.ResourceAlreadyExistException;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Category;
import com.cnpm.bottomcv.repository.CategoryRepository;
import com.cnpm.bottomcv.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistException("Category with slug " + request.getSlug() + " already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .build();

        category = categoryRepository.save(category);

        return maptoCategoryResponse(category);
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category id", "id", id.toString()));

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());

        return maptoCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category id", "id", id.toString()));
        return maptoCategoryResponse(category);
    }

    @Override
    public ListResponse<CategoryResponse> getAll(int pageNo, int pageSize, String sortBy, String sortType) {
        Sort sortObj = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<Category> pageCategory = categoryRepository.findAll(pageable);
        List<Category> categories = pageCategory.getContent();

        return ListResponse.<CategoryResponse>builder()
                .data(mapToCategoryListResponse(categories))
                .pageNo(pageCategory.getNumber())
                .pageSize(pageCategory.getSize())
                .totalElements((int) pageCategory.getTotalElements())
                .totalPages(pageCategory.getTotalPages())
                .isLast(pageCategory.isLast())
                .build();
    }

    private List<CategoryResponse> mapToCategoryListResponse(List<Category> categories) {
        return categories.stream()
                .map(this::maptoCategoryResponse)
                .toList();
    }

    private CategoryResponse maptoCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .build();
    }
}
