package com.tmeras.resellmart.product;

import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final FileService fileService;

    public ProductResponse save(ProductRequest productRequest, Authentication authentication) {
        productRequest.setId(null);
        User currentUser = (User) authentication.getPrincipal();

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findWithAssociationsById(currentUser.getId()).get();
        Category category = categoryRepository.findWithAssociationsById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("No category found with ID: " + productRequest.getCategoryId()));

        if (productRequest.getPrice() < productRequest.getDiscountedPrice())
            throw new APIException("Discounted price cannot be higher than regular price");

        // TODO: Quantity edge case -> Ensure quantity>0 on newly created items

        Product product = productMapper.toProduct(productRequest);
        product.setSeller(currentUser);
        product.setCategory(category);

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

        //"%" + keyword.toLowerCase() + "%"
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
        );    }

    public ProductResponse update(ProductRequest productRequest, Integer productId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Category category = categoryRepository.findWithAssociationsById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("No category found with ID: " + productRequest.getCategoryId()));
        Product existingproduct = productRepository.findWithAssociationsById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + productId));

        if (!Objects.equals(existingproduct.getSeller().getId(), currentUser.getId()))
            throw new OperationNotPermittedException("You do not have permission to update this product");

        if (productRequest.getPrice() < productRequest.getDiscountedPrice())
            throw new APIException("Discounted price cannot be higher than regular price");

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

    public ProductResponse uploadProductImages(
            List<MultipartFile> images, Integer productId, Authentication authentication
    ) throws IOException {
        User currentUser = (User) authentication.getPrincipal();
        Product existingproduct = productRepository.findWithAssociationsById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + productId));

        if (!Objects.equals(existingproduct.getSeller().getId(), currentUser.getId()))
            throw new OperationNotPermittedException("You do not have permission to upload images for this product");

        if (images.size() > AppConstants.MAX_IMAGE_NUMBER)
            throw new APIException("Maximum " + AppConstants.MAX_IMAGE_NUMBER + " images can be uploaded");

        for (MultipartFile image : images) {
            String fileName = image.getOriginalFilename();
            String fileExtension = fileService.getFileExtension(fileName);
            Set<String> validImageExtensions = Set.of("jpg", "jpeg", "png", "gif", "bmp", "tiff");
            if (!validImageExtensions.contains(fileExtension))
                throw new APIException("Only images can be uploaded");
        }

        // Delete any previous images
        for (ProductImage productImage: existingproduct.getImages())
            fileService.deleteFile(productImage.getFilePath());
        existingproduct.getImages().clear();

        // Save images
        List<String> filePaths = fileService.saveProductImages(images, existingproduct.getId());
        for (String filePath: filePaths) {
            ProductImage productImage = ProductImage.builder()
                    .filePath(filePath)
                    .displayed(false)
                    .build();
            existingproduct.getImages().add(productImage);
        }
        existingproduct.getImages().get(0).setDisplayed(true);

        Product updatedProduct = productRepository.save(existingproduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    public ProductResponse displayImage(Integer productId, Integer imageId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Product existingproduct = productRepository.findWithAssociationsById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + productId));
        ProductImage existingImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("No product image found with ID: " + imageId));

        if (!existingproduct.getImages().contains(existingImage))
            throw new APIException("The image is related to a different product");

        if (!Objects.equals(existingproduct.getSeller().getId(), currentUser.getId()))
            throw new OperationNotPermittedException("You do not have permission to manage images for this product");

        // Make displayed=false for all images of this product
        // before making displayed=true for the specified image
        for (ProductImage productImage: existingproduct.getImages())
            productImage.setDisplayed(false);
        int imageIndex = existingproduct.getImages().indexOf(existingImage);
        existingproduct.getImages().get(imageIndex).setDisplayed(true);

        Product updatedProduct = productRepository.save(existingproduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    @PreAuthorize("hasRole('ADMIN')") // Deletion possible by admins only, users can only mark products as unavailable
    public void delete(Integer productId) throws IOException {
        Optional<Product> existingProduct = productRepository.findWithImagesById(productId);
        if (existingProduct.isPresent() && existingProduct.get().getImages() != null)
            for (ProductImage productImage: existingProduct.get().getImages())
                fileService.deleteFile(productImage.getFilePath());

        productRepository.deleteById(productId);
    }
}
