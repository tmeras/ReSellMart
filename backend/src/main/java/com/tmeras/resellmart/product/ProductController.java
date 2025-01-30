package com.tmeras.resellmart.product;

import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
        // Get all products excluding those sold by the logged-in user
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
        // Get all products belonging to the specified category excluding those sold by the logged-in user
        PageResponse<ProductResponse> foundProducts =
                productService.findAllByCategoryId(pageNumber, pageSize, sortBy, sortDirection, categoryId, authentication);
        return new ResponseEntity<>(foundProducts, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductResponse>> findAllByKeyword(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection,
            @RequestParam(name = "keyword") String keyword,
            Authentication authentication
    ) {
        // Search for products based on the provided keyword excluding those sold by the logged-in user
        PageResponse<ProductResponse> foundProducts =
                productService.findAllByKeyword(pageNumber, pageSize, sortBy, sortDirection, keyword, authentication);
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

    @PutMapping(value = "/{product-id}/images", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadProductImages(
            @PathVariable(name = "product-id") Integer productId,
            @RequestPart("images") List<MultipartFile> images,
            Authentication authentication
    ) throws IOException {
        productService.uploadProductImages(images, productId, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{product-id}")
    public ResponseEntity<?> delete(
            @PathVariable(name = "product-id") Integer productId
    ) {
        productService.delete(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
