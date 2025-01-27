package com.tmeras.resellmart.category;

import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.ResourceAlreadyExistsException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    public CategoryResponse save(CategoryRequest categoryRequest) {
        categoryRequest.setId(null);

        if (categoryRepository.findByName(categoryRequest.getName()).isPresent())
            throw new ResourceAlreadyExistsException("A category with the name: " + categoryRequest.getName() + " already exists");

        Category category = categoryMapper.toCategory(categoryRequest);
        Category parentCategory = categoryRequest.getParentId() == null ? null :
                categoryRepository.findById(categoryRequest.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No parent category found with id: " + categoryRequest.getParentId())
                        );
        if (parentCategory != null && parentCategory.getParentCategory() != null)
            throw new APIException("Parent category should not have a parent");
        category.setParentCategory(parentCategory);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    public CategoryResponse findById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toCategoryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No category found with id:" + categoryId));
    }

    public PageResponse<CategoryResponse> findAll(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Category> categories= categoryRepository.findAll(pageable);
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();

        return new PageResponse<>(
                categoryResponses,
                categories.getNumber(),
                categories.getSize(),
                categories.getTotalElements(),
                categories.getTotalPages(),
                categories.isFirst(),
                categories.isLast()
        );
    }

    public PageResponse<CategoryResponse> findAllByParentId(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, Integer parentId
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Category> categories= categoryRepository.findAllByParentId(pageable, parentId);
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();

        return new PageResponse<>(
                categoryResponses,
                categories.getNumber(),
                categories.getSize(),
                categories.getTotalElements(),
                categories.getTotalPages(),
                categories.isFirst(),
                categories.isLast()
        );
    }

    public List<CategoryResponse> findAllParents() {
        List<Category> categories = categoryRepository.findAllParents();
        List<CategoryResponse> categoryResponses = categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();

        return categoryResponses;
    }

    public CategoryResponse update(CategoryRequest categoryRequest, Integer categoryId) {
        Category savedCategory = categoryRepository.findById(categoryId).orElseThrow(
                () ->new ResourceNotFoundException("No category found with id:" + categoryId));

        // Only allow updates to category name
        savedCategory.setName(categoryRequest.getName());

        Category updatedCategory = categoryRepository.save(savedCategory);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    public void delete(Integer categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
