package com.tmeras.resellmart.product;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import com.tmeras.resellmart.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    public static final MockMultipartFile TEST_IMAGE_1;
    public static final MockMultipartFile TEST_IMAGE_2;

    static {
        try {
            TEST_IMAGE_1 = new MockMultipartFile(
                    "image1", "test_image_1.jpeg", "image/jpeg",
                    Files.readAllBytes(Path.of("src/test/resources/test_image_1.jpeg"))
            );
            TEST_IMAGE_2 = new MockMultipartFile(
                    "image2", "test_image_2.jpeg", "image/jpeg",
                    Files.readAllBytes(Path.of("src/test/resources/test_image_2.jpeg"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ProductService productService;

    private Product productA;
    private Product productB;
    private ProductRequest productRequestA;
    private ProductUpdateRequest productUpdateRequestA;
    private ProductResponse productResponseA;
    private ProductResponse productResponseB;
    private User userA;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        // Initialise test objects
        Role adminRole = new Role(1, "ADMIN");
        Role userRole = new Role(2, "USER");
        userA = TestDataUtils.createUserA(Set.of(adminRole));
        User userB = TestDataUtils.createUserB(Set.of(userRole));
        UserResponse userResponseA = TestDataUtils.createUserResponseA(Set.of(adminRole));
        UserResponse userResponseB = TestDataUtils.createUserResponseB(Set.of(userRole));

        Category category = TestDataUtils.createCategoryA();
        productA = TestDataUtils.createProductA(category, userA);
        productB = TestDataUtils.createProductB(category, userB);
        productRequestA = TestDataUtils.createProductRequestA(category.getId());
        productUpdateRequestA = ProductUpdateRequest.builder()
                .name("Updated test product A")
                .description("Updated description A")
                .price(BigDecimal.valueOf(10.0))
                .productCondition(ProductCondition.NEW)
                .availableQuantity(2)
                .categoryId(productRequestA.getCategoryId())
                .isDeleted(false)
                .build();

        CategoryResponse categoryResponse = TestDataUtils.createCategoryResponseA();
        productResponseA = TestDataUtils.createProductResponseA(categoryResponse, userResponseA);
        productResponseB = TestDataUtils.createProductResponseB(categoryResponse, userResponseB);

        authentication = new UsernamePasswordAuthenticationToken(
                userA, userA.getPassword(), userA.getAuthorities()
        );
    }

    @Test
    public void shouldSaveProductWhenValidRequest() {
        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(categoryRepository.findWithAssociationsById(productRequestA.getCategoryId()))
                .thenReturn(Optional.of(productA.getCategory()));
        when(productMapper.toProduct(productRequestA)).thenReturn(productA);
        when(productRepository.save(productA)).thenReturn(productA);
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        ProductResponse productResponse = productService.save(productRequestA, authentication);

        assertThat(productResponse).isEqualTo(productResponseA);
    }

    @Test
    public void shouldNotSaveProductWhenInvalidCategoryId() {
        productRequestA.setCategoryId(99);

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(categoryRepository.findWithAssociationsById(productRequestA.getCategoryId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.save(productRequestA, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No category found with ID: 99");
    }

    @Test
    public void shouldNotSaveProductWhenInvalidQuantity() {
        productRequestA.setAvailableQuantity(0);

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(categoryRepository.findWithAssociationsById(productRequestA.getCategoryId()))
                .thenReturn(Optional.of(productA.getCategory()));

        assertThatThrownBy(() -> productService.save(productRequestA, authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("Quantity of newly created product must be greater than be 0");
    }

    @Test
    public void shouldFindProductByIdWhenValidProductId() {
        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        ProductResponse productResponse = productService.findById(productRequestA.getId());

        assertThat(productResponse).isEqualTo(productResponseA);
    }

    @Test
    public void shouldNotFindProductByIdWhenInvalidProductId() {
        when(productRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product found with ID: 99");
    }

    @Test
    public void shouldFindAllProducts() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_PRODUCTS_BY).ascending() : Sort.by(AppConstants.SORT_PRODUCTS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Product> page = new PageImpl<>(List.of(productA, productB));

        when(productRepository.findAll(pageable)).thenReturn(page);
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);
        when(productMapper.toProductResponse(productB)).thenReturn(productResponseB);

        PageResponse<ProductResponse> pageResponse =
                productService.findAll(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR);

        assertThat(pageResponse.getContent().size()).isEqualTo(2);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(productResponseA);
        assertThat(pageResponse.getContent().get(1)).isEqualTo(productResponseB);
    }

    @Test
    public void shouldFindAllProductsExceptSellerProducts() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_PRODUCTS_BY).ascending() : Sort.by(AppConstants.SORT_PRODUCTS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Product> page = new PageImpl<>(List.of(productB));

        when(productRepository.findAllBySellerIdNot(pageable, productA.getSeller().getId())).thenReturn(page);
        when(productMapper.toProductResponse(productB)).thenReturn(productResponseB);

        PageResponse<ProductResponse> pageResponse =
                productService.findAllExceptSellerProducts(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR, authentication);

        assertThat(pageResponse.getContent().size()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(productResponseB);
    }

    @Test
    public void shouldFindAllProductsBySellerId() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_PRODUCTS_BY).ascending() : Sort.by(AppConstants.SORT_PRODUCTS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Product> page = new PageImpl<>(List.of(productA));

        when(productRepository.findAllBySellerId(pageable, productA.getSeller().getId())).thenReturn(page);
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        PageResponse<ProductResponse> pageResponse =
                productService.findAllBySellerId(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR, productA.getSeller().getId());

        assertThat(pageResponse.getContent().size()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(productResponseA);
    }

    @Test
    public void shouldFindAllProductsByCategoryId() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_PRODUCTS_BY).ascending() : Sort.by(AppConstants.SORT_PRODUCTS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Product> page = new PageImpl<>(List.of(productB));

        when(productRepository.findAllByCategoryId(pageable, productA.getCategory().getId(), productA.getSeller().getId())).thenReturn(page);
        when(productMapper.toProductResponse(productB)).thenReturn(productResponseB);

        PageResponse<ProductResponse> pageResponse =
                productService.findAllByCategoryId(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR, productA.getCategory().getId(), authentication);

        assertThat(pageResponse.getContent().size()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(productResponseB);
    }

    @Test
    public void shouldFindAllProductsByKeyword() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_PRODUCTS_BY).ascending() : Sort.by(AppConstants.SORT_PRODUCTS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Product> page = new PageImpl<>(List.of(productB));

        when(productRepository.findAllByKeyword(pageable, "Test product", userA.getId())).thenReturn(page);
        when(productMapper.toProductResponse(productB)).thenReturn(productResponseB);

        PageResponse<ProductResponse> pageResponse =
                productService.findAllByKeyword(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR, "Test product", authentication);

        assertThat(pageResponse.getContent().size()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(productResponseB);
    }

    @Test
    public void shouldFindAllProductsBySellerIdAndKeyword() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_PRODUCTS_BY).ascending() : Sort.by(AppConstants.SORT_PRODUCTS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Product> page = new PageImpl<>(List.of(productA));

        when(productRepository.findAllBySellerIdAndKeyword(pageable, productA.getSeller().getId(), "Test product")).thenReturn(page);
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        PageResponse<ProductResponse> pageResponse =
                productService.findAllBySellerIdAndKeyword(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR, productA.getSeller().getId(), "Test product");

        assertThat(pageResponse.getContent().size()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(productResponseA);
    }

    @Test
    public void shouldFindAllProductsByCategoryIdAndKeyword() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_PRODUCTS_BY).ascending() : Sort.by(AppConstants.SORT_PRODUCTS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Product> page = new PageImpl<>(List.of(productB));

        when(productRepository.findAllByCategoryIdAndKeyword(pageable, productA.getCategory().getId(), "Test product",
                userA.getId())).thenReturn(page);
        when(productMapper.toProductResponse(productB)).thenReturn(productResponseB);

        PageResponse<ProductResponse> pageResponse =
                productService.findAllByCategoryIdAndKeyword(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR, productA.getCategory().getId(), "Test product", authentication);

        assertThat(pageResponse.getContent().size()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(productResponseB);
    }

    @Test
    public void shouldUpdateProductWhenValidRequest() {
        productResponseA.setName("Updated test product A");
        productRequestA.setDescription("Updated description A");

        when(categoryRepository.findWithAssociationsById(productA.getCategory().getId()))
                .thenReturn(Optional.ofNullable(productA.getCategory()));
        when(productRepository.findWithAssociationsById(productA.getId()))
                .thenReturn(Optional.ofNullable(productA));
        when(productRepository.save(productA)).thenReturn(productA);
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        ProductResponse productResponse = productService.update(productUpdateRequestA, productA.getId(), authentication);

        assertThat(productResponse).isEqualTo(productResponseA);
        assertThat(productA.getName()).isEqualTo("Updated test product A");
        assertThat(productA.getDescription()).isEqualTo("Updated description A");
    }

    @Test
    public void shouldNotUpdateProductWhenInvalidCategoryId() {
        productUpdateRequestA.setCategoryId(99);

        when(productRepository.findWithAssociationsById(productA.getId()))
                .thenReturn(Optional.of(productA));
        when(categoryRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(productUpdateRequestA, productA.getId(), authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No category found with ID: 99");
    }

    @Test
    public void shouldNotUpdateProductWhenInvalidProductId() {
        when(productRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(productUpdateRequestA, 99, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product found with ID: 99");
    }

    @Test
    public void shouldNotUpdateProductWhenSellerIsNotLoggedIn() {
        authentication = new UsernamePasswordAuthenticationToken(
                productB.getSeller(),
                productB.getSeller().getPassword(),
                productB.getSeller().getAuthorities()
        );

        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.ofNullable(productA));

        assertThatThrownBy(() -> productService.update(productUpdateRequestA, productRequestA.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to update this product");
    }

    @Test
    public void shouldUploadProductImagesWhenValidRequest() throws IOException {
        List<MultipartFile> images = List.of(TEST_IMAGE_1);
        productResponseA.setImages(List.of(
                new ProductImageResponse(
                        1,
                        TEST_IMAGE_1.getOriginalFilename(),
                        TEST_IMAGE_1.getContentType(),
                        TEST_IMAGE_1.getBytes()
                )
        ));

        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));
        when(fileService.getFileExtension("test_image_1.jpeg")).thenReturn("jpeg");
        when(fileService.saveProductImages(images, productA.getId()))
                .thenReturn(List.of("/uploads/test_image_1.jpeg"));
        when(productRepository.save(productA)).thenReturn(productA);
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        ProductResponse productResponse = productService.uploadProductImages(images, productRequestA.getId(), authentication);

        assertThat(productResponse).isEqualTo(productResponseA);
        assertThat(productA.getImages().size()).isEqualTo(1);
        assertThat(productA.getImages().get(0).getImagePath()).isEqualTo("/uploads/test_image_1.jpeg");
    }

    @Test
    public void shouldNotUploadProductImagesWhenInvalidProductId() {
        List<MultipartFile> images = List.of(TEST_IMAGE_1);

        when(productRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.uploadProductImages(images, 99, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product found with ID: 99");
    }

    @Test
    public void shouldNotUploadProductImagesWhenSellerIsNotLoggedIn() {
        List<MultipartFile> images = List.of(TEST_IMAGE_1);

        authentication = new UsernamePasswordAuthenticationToken(
                productB.getSeller(),
                productB.getSeller().getPassword(),
                productB.getSeller().getAuthorities()
        );

        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));

        assertThatThrownBy(() -> productService.uploadProductImages(images, productA.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to upload images for this product");
    }

    @Test
    public void shouldNotUploadProductImagesWhenImageLimitExceeded() {
        List<MultipartFile> images = Collections.nCopies(6, TEST_IMAGE_1);

        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));

        assertThatThrownBy(() -> productService.uploadProductImages(images, productA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("Maximum 5 images can be uploaded");
    }

    @Test
    public void shouldNotUploadProductImageWhenInvalidFileExtension() throws IOException {
        List<MultipartFile> images = List.of(
                new MockMultipartFile(
                        "images", "test_file.txt", "text/plain",
                        Files.readAllBytes(Paths.get("src/test/resources/test_file.txt"))
                )
        );

        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));
        when(fileService.getFileExtension("test_file.txt")).thenReturn("txt");

        assertThatThrownBy(() -> productService.uploadProductImages(images, productA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("Only images can be uploaded");
    }

    @Test
    public void shouldFindPrimaryProductImageWhenValidRequest() throws IOException {
        ProductImage productImage = new ProductImage(
                1,
                TEST_IMAGE_1.getOriginalFilename(),
                TEST_IMAGE_1.getContentType(),
                "src/test/resources/test_image_1.jpeg"
        );
        productA.getImages().add(productImage);
        ProductImageResponse productImageResponse = new ProductImageResponse(
                1,
                TEST_IMAGE_1.getOriginalFilename(),
                TEST_IMAGE_1.getContentType(),
                TEST_IMAGE_1.getBytes()
        );

        when(productRepository.findWithImagesById(productA.getId())).thenReturn(Optional.of(productA));
        when(productMapper.toProductImageResponse(productImage)).thenReturn(productImageResponse);

        ProductImageResponse response = productService.findPrimaryProductImage(productA.getId());

        assertThat(response).isEqualTo(productImageResponse);
    }

    @Test
    public void shouldNotFindPrimaryProductImageWhenInvalidProductId() {
        when(productRepository.findWithImagesById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findPrimaryProductImage(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product found with ID: 99");
    }

    @Test
    public void shouldNotFindPrimaryProductImageWhenEmptyImagesList() {
        when(productRepository.findWithImagesById(productA.getId())).thenReturn(Optional.of(productA));

        assertThatThrownBy(() -> productService.findPrimaryProductImage(productA.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No images found for product with ID: " + productA.getId());
    }

    @Test
    public void shouldDeleteProduct() {
        when(productRepository.findById(productA.getId())).thenReturn(Optional.of(productA));

        productService.delete(productA.getId());

        assertThat(productA.getIsDeleted()).isTrue();
    }

    @Test
    public void shouldNotDeleteProductWhenInvalidProductId() {
        when(productRepository.findById(productA.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(productA.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product found with ID: " + productA.getId());
    }
}
