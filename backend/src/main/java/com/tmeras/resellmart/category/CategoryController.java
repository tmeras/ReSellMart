package com.tmeras.resellmart.category;

import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> save(
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        CategoryResponse savedCategory = categoryService.save(categoryRequest);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @GetMapping("/{category-id}")
    public ResponseEntity<CategoryResponse> findById(
            @PathVariable("category-id") Integer categoryId
    ) {
        CategoryResponse foundCategory = categoryService.findById(categoryId);
        return new ResponseEntity<>(foundCategory, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> findAll(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
    ) {
        PageResponse<CategoryResponse> foundCategories =
                categoryService.findAll(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(foundCategories, HttpStatus.OK);
    }

    @GetMapping("/parents/{parent-id}")
    public ResponseEntity<PageResponse<CategoryResponse>> findAllByParentId(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection,
            @PathVariable(name = "parent-id") Integer parentId
    ) {
        PageResponse<CategoryResponse> foundCategories =
                categoryService.findAllByParentId(pageNumber, pageSize, sortBy, sortDirection, parentId);
        return new ResponseEntity<>(foundCategories, HttpStatus.OK);
    }

    @GetMapping("/parents")
    public ResponseEntity<List<CategoryResponse>> findAllParents() {
        List<CategoryResponse> foundParents = categoryService.findAllParents();
        return new ResponseEntity<>(foundParents, HttpStatus.OK);
    }

    @PutMapping("/{category-id}")
    public ResponseEntity<CategoryResponse> update(
            @Valid @RequestBody CategoryRequest categoryRequest,
            @PathVariable(name = "category-id") Integer categoryId
    ) {
        CategoryResponse updatedCategory = categoryService.update(categoryRequest, categoryId);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

    @DeleteMapping({"/{category-id}"})
    public ResponseEntity<?> delete(
            @PathVariable(name = "category-id") Integer categoryId
    ) {
        categoryService.delete(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
