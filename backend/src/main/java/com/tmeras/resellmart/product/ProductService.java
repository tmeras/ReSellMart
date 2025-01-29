package com.tmeras.resellmart.product;

import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductResponse save(ProductRequest productRequest, Authentication authentication) {
        productRequest.setId(null);
        User currentUser = (User) authentication.getPrincipal();

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("No category found with ID " + productRequest.getCategoryId()));

        if (productRequest.getPrice() < productRequest.getDiscountedPrice())
            throw new APIException("Discounted price cannot be higher than regular price");

        Product product = productMapper.toProduct(productRequest);
        product.setSeller(currentUser);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    public ProductResponse findById(Integer productId) {
        return productRepository.findById(productId)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID " + productId));
    }

    @PreAuthorize("hasRole('ADMIN')") //Only admins should be able to view both available and unavailable products
    public PageResponse<ProductResponse> findAll(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> products = productRepository.findAll(pageable);
        List<ProductResponse> productResponses = products.stream()
                .map(productMapper::toProductResponse)
                .toList();

        return new PageResponse<>(
                productResponses,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isFirst(),
                products.isLast()
        );
    }

    public PageResponse<ProductResponse> findAllExceptSeller(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> products = productRepository.findAllBySellerIdNot(pageable, currentUser.getId());
        List<ProductResponse> productResponses = products.stream()
                .map(productMapper::toProductResponse)
                .toList();

        return new PageResponse<>(
                productResponses,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isFirst(),
                products.isLast()
        );
    }

    public PageResponse<ProductResponse> findAllBySellerId(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, Integer sellerId
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> products = productRepository.findAllBySellerId(pageable, sellerId);
        List<ProductResponse> productResponses = products.stream()
                .map(productMapper::toProductResponse)
                .toList();

        return new PageResponse<>(
                productResponses,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isFirst(),
                products.isLast()
        );
    }

    public PageResponse<ProductResponse> findAllByCategoryId(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, Integer categoryId, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> products = productRepository.findAllByCategoryId(pageable, categoryId, currentUser.getId());
        List<ProductResponse> productResponses = products.stream()
                .map(productMapper::toProductResponse)
                .toList();

        return new PageResponse<>(
                productResponses,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isFirst(),
                products.isLast()
        );
    }

    // TODO: Search??

    public ProductResponse update(ProductRequest productRequest, Integer productId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("No category found with ID " + productRequest.getCategoryId()));
        Product existingproduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID " + productId));

        if (!Objects.equals(existingproduct.getSeller().getId(), currentUser.getId()))
            throw new OperationNotPermittedException("You do not have permission to update this product");

        existingproduct.setName(productRequest.getName());
        existingproduct.setDescription(productRequest.getDescription());
        existingproduct.setPrice(productRequest.getPrice());
        existingproduct.setDiscountedPrice(productRequest.getDiscountedPrice());
        existingproduct.setProductCondition(productRequest.getProductCondition());
        existingproduct.setAvailableQuantity(productRequest.getAvailableQuantity());
        existingproduct.setAvailable(productRequest.isAvailable());
        existingproduct.setCategory(category);

        Product updatedProduct = productRepository.save(existingproduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    @PreAuthorize("hasRole('ADMIN')") // Deletion possible by admins only, users can only mark products as unavailable
    public void delete(Integer productId) {
        productRepository.deleteById(productId);
    }
}
