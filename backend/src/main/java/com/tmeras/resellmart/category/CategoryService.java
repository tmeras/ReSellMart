package com.tmeras.resellmart.category;

import com.tmeras.resellmart.exception.ResourceNotFoundException;
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
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Parent category", "id", categoryRequest.getParentId())
                        );
        category.setParentCategory(parentCategory);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    public CategoryResponse findById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toCategoryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }
}
