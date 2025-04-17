package com.tmeras.resellmart.product;

import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.ForeignKeyConstraintException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.order.OrderItemRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.tmeras.resellmart.common.AppConstants.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductMapper productMapper;
    private final FileService fileService;

    public ProductResponse save(ProductRequest productRequest, Authentication authentication) {
        productRequest.setId(null);
        User currentUser = (User) authentication.getPrincipal();

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findWithAssociationsById(currentUser.getId()).get();
        Category category = categoryRepository.findWithAssociationsById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("No category found with ID: " + productRequest.getCategoryId()));

        if (productRequest.getAvailableQuantity() == 0)
            throw new APIException("Quantity of newly created product must be greater than be 0");

        Product product = productMapper.toProduct(productRequest);
        product.setSeller(currentUser);
        product.setCategory(category);
        product.setIsDeleted(false);

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    public ProductResponse findById(Integer productId) {
        return productRepository.findWithAssociationsById(productId)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + productId));
    }

    @PreAuthorize("hasRole('ADMIN')") //Only admins should be able to view both available and unavailable products
    public PageResponse<ProductResponse> findAll(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> products = productRepository.findAll(pageable);
        // Initialize lazy associations
        for(Product product : products) {
            product.getImages().size();
            product.getSeller().getRoles().size();
        }
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

    public PageResponse<ProductResponse> findAllExceptSellerProducts(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> products = productRepository.findAllBySellerIdNot(pageable, currentUser.getId());
        // Initialize lazy associations
        for(Product product : products) {
            product.getImages().size();
            product.getSeller().getRoles().size();
        }
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
        // Initialize lazy associations
        for(Product product : products) {
            product.getImages().size();
            product.getSeller().getRoles().size();
        }
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
        // Initialize lazy associations
        for(Product product : products) {
            product.getImages().size();
            product.getSeller().getRoles().size();
        }
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

    public PageResponse<ProductResponse> findAllByKeyword(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String keyword, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> products = productRepository.findAllByKeyword(pageable, keyword, currentUser.getId());
        // Initialize lazy associations
        for(Product product : products) {
            product.getImages().size();
            product.getSeller().getRoles().size();
        }
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

    public PageResponse<ProductResponse> findAllBySellerIdAndKeyword(
        Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, Integer sellerId, String keyword
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> products = productRepository.findAllBySellerIdAndKeyword(pageable, sellerId, keyword);
        // Initialize lazy associations
        for(Product product : products) {
            product.getImages().size();
            product.getSeller().getRoles().size();
        }
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

    public PageResponse<ProductResponse> findAllByCategoryIdAndKeyword(
            Integer pageNumber, Integer pageSize, String sortBy, String sortDirection,
            Integer categoryId, String keyword, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> products = productRepository.findAllByCategoryIdAndKeyword(pageable, categoryId, keyword, currentUser.getId());
        // Initialize lazy associations
        for (Product product : products) {
            product.getImages().size();
            product.getSeller().getRoles().size();
        }
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

    public ProductResponse update(ProductUpdateRequest productRequest, Integer productId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Product existingProduct = productRepository.findWithAssociationsById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + productId));

        if (!Objects.equals(existingProduct.getSeller().getId(), currentUser.getId()))
            throw new OperationNotPermittedException("You do not have permission to update this product");

        if (productRequest.getCategoryId() != null)
        {
            Category category = categoryRepository.findWithAssociationsById(productRequest.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("No category found with ID: " + productRequest.getCategoryId()));
            existingProduct.setCategory(category);
        }

        if (existingProduct.getName() != null)
            existingProduct.setName(productRequest.getName());

        if (productRequest.getDescription() != null)
            existingProduct.setDescription(productRequest.getDescription());

        if (productRequest.getPrice() != null) {
            if (!productRequest.getPrice().equals(existingProduct.getPrice()))
                existingProduct.setPreviousPrice(existingProduct.getPrice());
            existingProduct.setPrice(productRequest.getPrice());
        }

        if (productRequest.getProductCondition() != null)
            existingProduct.setProductCondition(productRequest.getProductCondition());

        if (productRequest.getAvailableQuantity() != null)
            existingProduct.setAvailableQuantity(productRequest.getAvailableQuantity());

        if (productRequest.getIsDeleted() != null)
            existingProduct.setIsDeleted(productRequest.getIsDeleted());

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    public ProductResponse uploadProductImages(
            List<MultipartFile> images, Integer productId, Authentication authentication
    ) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        Product existingProduct = productRepository.findWithAssociationsById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + productId));

        if (!Objects.equals(existingProduct.getSeller().getId(), currentUser.getId()))
            throw new OperationNotPermittedException("You do not have permission to upload images for this product");

        if (images.size() > MAX_IMAGE_NUMBER)
            throw new APIException("Maximum " + MAX_IMAGE_NUMBER + " images can be uploaded");

        for (MultipartFile image : images) {
            String fileName = image.getOriginalFilename();
            String fileExtension = fileService.getFileExtension(fileName);
            if (!ACCEPTED_IMAGE_EXTENSIONS.contains(fileExtension))
                throw new APIException("Only images can be uploaded");
        }

        // Delete any previous images if product wasn't created using flyway script
        if (productId > FLYWAY_PRODUCTS_NUMBER)
            for (ProductImage productImage: existingProduct.getImages())
                fileService.deleteFile(productImage.getFilePath());
        existingProduct.getImages().clear();

        // Save images
        List<String> filePaths = fileService.saveProductImages(images, existingProduct.getId());
        for (int i = 0; i < images.size(); i++) {
            ProductImage productImage = ProductImage.builder()
                    .name(images.get(i).getOriginalFilename())
                    .type(images.get(i).getContentType())
                    .filePath(filePaths.get(i))
                    .build();
            existingProduct.getImages().add(productImage);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    @PreAuthorize("hasRole('ADMIN')") // Deletion possible by admins only, users can only mark products as unavailable
    public void delete(Integer productId) throws IOException {
        if (orderItemRepository.existsByProductId(productId))
            throw new ForeignKeyConstraintException("Cannot delete product due to existing orders that reference it");

        Optional<Product> existingProduct = productRepository.findWithImagesById(productId);

        // Delete images only if the product was not created using flyway script
        if (productId > FLYWAY_PRODUCTS_NUMBER && existingProduct.isPresent() &&
                existingProduct.get().getImages() != null
        ) {
            for (ProductImage productImage : existingProduct.get().getImages()) {
                fileService.deleteFile(productImage.getFilePath());
            }
        }

        productRepository.deleteById(productId);
    }
}
