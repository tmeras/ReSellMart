package com.tmeras.resellmart.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> saveCategory(
            @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        CategoryResponse savedCategory = categoryService.save(categoryRequest);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @GetMapping("{category-id}")
    public ResponseEntity<CategoryResponse> findCategoryById(
            @PathVariable("category-id") Integer categoryId
    ) {
        return new ResponseEntity<>(categoryService.findById(categoryId), HttpStatus.OK);
    }

}
