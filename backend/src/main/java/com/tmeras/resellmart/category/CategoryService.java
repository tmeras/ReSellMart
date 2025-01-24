package com.tmeras.resellmart.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    public CategoryResponse save(CategoryRequest categoryRequest) {
        categoryRequest.setId(null);

        Category category = categoryMapper.toCategory(categoryRequest);
        Category parentCategory = categoryRequest.getParentId() == null ? null :
                categoryRepository.findById(categoryRequest.getParentId())
                        .orElseThrow(() -> new RuntimeException("Parent category with id " +
                                categoryRequest.getParentId() + " not found")); //TODO: Custom exceptions
        category.setParentCategory(parentCategory);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    public CategoryResponse findById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toCategoryResponse)
                .orElseThrow(() -> new RuntimeException("Category with id " + categoryId + " not found"));
    }
}
