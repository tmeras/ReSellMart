package com.tmeras.resellmart.category;

import org.springframework.stereotype.Service;

@Service
public class CategoryMapper {

    public Category toCategory(CategoryRequest categoryRequest) {
        return Category.builder()
                .id(categoryRequest.getId())
                .name(categoryRequest.getName())
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        Integer parentId = category.getParentCategory() == null ? null
                        : category.getParentCategory().getId();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(parentId)
                .build();
    }
}
