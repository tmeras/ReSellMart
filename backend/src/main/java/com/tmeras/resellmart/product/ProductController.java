package com.tmeras.resellmart.product;

import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> save(
            @Valid @RequestBody ProductRequest productRequest,
            Authentication authentication
    ) {
        ProductResponse savedProduct = productService.save(productRequest, authentication);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{product-id}")
    public ResponseEntity<ProductResponse> findById(
            @PathVariable("product-id") Integer productId
    ) {
        ProductResponse foundProduct = productService.findById(productId);
        return new ResponseEntity<>(foundProduct, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> findAll(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
    ) {
        PageResponse<ProductResponse> foundProducts =
                productService.findAll(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(foundProducts, HttpStatus.OK);
    }

    @GetMapping("/others")
    public ResponseEntity<PageResponse<ProductResponse>> findAllExceptSeller(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection,
            Authentication authentication
    ) {
        // Get all products except those sold by the logged-in user
        PageResponse<ProductResponse> foundProducts =
                productService.findAllExceptSeller(pageNumber, pageSize, sortBy, sortDirection, authentication);
        return new ResponseEntity<>(foundProducts, HttpStatus.OK);
    }

    @GetMapping("/user/{seller-id}")
    public ResponseEntity<PageResponse<ProductResponse>> findAllBySellerId(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection,
            @PathVariable(name = "seller-id") Integer sellerId
    ) {
        PageResponse<ProductResponse> foundProducts =
                productService.findAllBySellerId(pageNumber, pageSize, sortBy, sortDirection, sellerId);
        return new ResponseEntity<>(foundProducts, HttpStatus.OK);
    }

    @GetMapping("/category/{category-id}")
    public ResponseEntity<PageResponse<ProductResponse>> findAllByCategoryId(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection,
            @PathVariable(name = "category-id") Integer categoryId,
            Authentication authentication
    ) {
        PageResponse<ProductResponse> foundProducts =
                productService.findAllByCategoryId(pageNumber, pageSize, sortBy, sortDirection, categoryId, authentication);
        return new ResponseEntity<>(foundProducts, HttpStatus.OK);
    }

    @PutMapping("/{product-id}")
    public ResponseEntity<ProductResponse> update(
            @Valid @RequestBody ProductRequest productRequest,
            @PathVariable(name = "product-id") Integer productId,
            Authentication authentication
    ) {
        ProductResponse updatedProduct = productService.update(productRequest, productId, authentication);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @DeleteMapping("/{product-id}")
    public ResponseEntity<?> delete(
            @PathVariable(name = "product-id") Integer productId
    ) {
        productService.delete(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
