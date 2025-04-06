package com.tmeras.resellmart.category;

import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.ForeignKeyConstraintException;
import com.tmeras.resellmart.exception.ResourceAlreadyExistsException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse save(CategoryRequest categoryRequest) {
        categoryRequest.setId(null);

        if (categoryRepository.findByName(categoryRequest.getName()).isPresent())
            throw new ResourceAlreadyExistsException("A category with the name: '" + categoryRequest.getName() + "' already exists");

        Category parentCategory = categoryRequest.getParentId() == null ? null :
                categoryRepository.findById(categoryRequest.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No parent category found with ID: " + categoryRequest.getParentId())
                        );
        if (parentCategory != null && parentCategory.getParentCategory() != null)
            throw new APIException("Parent category should not have a parent");

        Category category = categoryMapper.toCategory(categoryRequest);
        category.setParentCategory(parentCategory);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    public CategoryResponse findById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryMapper::toCategoryResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No category found with ID: " + categoryId));
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

        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse update(CategoryRequest categoryRequest, Integer categoryId) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() ->new ResourceNotFoundException("No category found with ID: " + categoryId));

        // Only allow updates to category name
        existingCategory.setName(categoryRequest.getName());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Integer categoryId) {
        if (productRepository.existsByCategoryId(categoryId))
            throw new ForeignKeyConstraintException("Cannot delete category due to existing products that reference it");

        categoryRepository.deleteById(categoryId);
    }
}
