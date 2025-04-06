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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    public static final Path TEST_IMAGE_PATH_1 = Paths.get("src/test/resources/test_image_1.jpeg");
    public static final Path TEST_IMAGE_PATH_2 = Paths.get("src/test/resources/test_image_2.jpeg");

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
        productRequestA.setName("Updated product name");
        productRequestA.setDescription("Updated product description");
        productResponseA.setName("Updated product name");
        productResponseA.setDescription("Updated product description");

        when(categoryRepository.findWithAssociationsById(productA.getCategory().getId()))
                .thenReturn(Optional.ofNullable(productA.getCategory()));
        when(productRepository.findWithAssociationsById(productA.getId()))
                .thenReturn(Optional.ofNullable(productA));
        when(productRepository.save(productA)).thenReturn(productA);
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        ProductResponse productResponse = productService.update(productRequestA, productA.getId(), authentication);

        assertThat(productResponse).isEqualTo(productResponseA);
        assertThat(productA.getName()).isEqualTo("Updated product name");
        assertThat(productA.getDescription()).isEqualTo("Updated product description");
    }

    @Test
    public void shouldNotUpdateProductWhenInvalidCategoryId() {
        productRequestA.setCategoryId(99);

        when(categoryRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(productRequestA, productA.getId(), authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No category found with ID: 99");
    }

    @Test
    public void shouldNotUpdateProductWhenInvalidProductId() {
        when(categoryRepository.findWithAssociationsById(productA.getCategory().getId()))
                .thenReturn(Optional.ofNullable(productA.getCategory()));
        when(productRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(productRequestA, 99, authentication))
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

        when(categoryRepository.findWithAssociationsById(productA.getCategory().getId()))
                .thenReturn(Optional.ofNullable(productA.getCategory()));
        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.ofNullable(productA));

        assertThatThrownBy(() -> productService.update(productRequestA, productRequestA.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to update this product");
    }

    @Test
    public void shouldUploadProductImagesWhenValidRequest() throws IOException {
        List<MultipartFile> images = List.of(
                new MockMultipartFile(
                        "images", "test_image_1.jpeg", "image/jpeg",
                        Files.readAllBytes(TEST_IMAGE_PATH_1)
                )
        );
        productResponseA.setImages(List.of(
                new ProductImageResponse(
                        1,
                        Files.readAllBytes(TEST_IMAGE_PATH_1),
                        false
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
        assertThat(productA.getImages().get(0).getFilePath()).isEqualTo("/uploads/test_image_1.jpeg");
    }

    @Test
    public void shouldNotUploadProductImagesWhenInvalidProductId() throws IOException {
        List<MultipartFile> images = List.of(
                new MockMultipartFile(
                        "images", "test_image_1.jpeg", "image/jpeg",
                        Files.readAllBytes(TEST_IMAGE_PATH_1)
                )
        );

        when(productRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.uploadProductImages(images, 99, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product found with ID: 99");
    }

    @Test
    public void shouldNotUploadProductImagesWhenSellerIsNotLoggedIn() throws IOException {
        List<MultipartFile> images = List.of(
                new MockMultipartFile(
                        "images", "test_image_1.jpeg", "image/jpeg",
                        Files.readAllBytes(TEST_IMAGE_PATH_1)
                )
        );

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
    public void shouldNotUploadProductImagesWhenImageLimitExceeded() throws IOException {
        List<MultipartFile> images = List.of(
                new MockMultipartFile(
                        "images", "test_image_1.jpeg", "image/jpeg",
                        Files.readAllBytes(TEST_IMAGE_PATH_1)
                ),
                new MockMultipartFile(
                        "images", "test_image_1.jpeg", "image/jpeg",
                        Files.readAllBytes(TEST_IMAGE_PATH_1)
                ),
                new MockMultipartFile(
                        "images", "test_image_1.jpeg", "image/jpeg",
                        Files.readAllBytes(TEST_IMAGE_PATH_1)
                ),
                new MockMultipartFile(
                        "images", "test_image_1.jpeg", "image/jpeg",
                        Files.readAllBytes(TEST_IMAGE_PATH_1)
                ),
                new MockMultipartFile(
                        "images", "test_image_1.jpeg", "image/jpeg",
                        Files.readAllBytes(TEST_IMAGE_PATH_1)
                ),
                new MockMultipartFile(
                        "images", "test_image_1.jpeg", "image/jpeg",
                        Files.readAllBytes(TEST_IMAGE_PATH_1)
                )
        );

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
    public void shouldDisplayImageWhenValidRequest() throws IOException {
        productResponseA.setImages(List.of(
                new ProductImageResponse(
                        1,
                        Files.readAllBytes(TEST_IMAGE_PATH_1),
                        true
                ),
                new ProductImageResponse(
                        2,
                        Files.readAllBytes(TEST_IMAGE_PATH_2),
                        false
                )
        ));
        ProductImage productImageA = new ProductImage(1, "/uploads/test_image_1.jpeg", false);
        ProductImage productImageB = new ProductImage(2, "/uploads/test_image_2.jpeg", true);
        productA.getImages().addAll(List.of(productImageA, productImageB));

        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));
        when(productImageRepository.findById(productImageA.getId())).thenReturn(Optional.of(productImageA));
        when(productRepository.save(productA)).thenReturn(productA);
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        ProductResponse productResponse =
                productService.displayImage(productA.getId(), productImageA.getId(), authentication);

        assertThat(productResponse).isEqualTo(productResponseA);
        assertThat(productA.getImages().get(0).isDisplayed()).isTrue();
        assertThat(productA.getImages().get(1).isDisplayed()).isFalse();
    }

    @Test
    public void shouldNotDisplayImageWhenInvalidProductId() {
        when(productRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.displayImage(99, 1, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product found with ID: 99");
    }

    @Test
    public void shouldNotDisplayImageWhenInvalidImageId() {
        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));
        when(productImageRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.displayImage(productA.getId(), 99, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product image found with ID: 99");
    }

    @Test
    public void shouldNotDisplayImageWhenImageBelongsToDifferentProduct() {
        ProductImage productImageA = new ProductImage(1, "/uploads/test_image_1.jpeg", false);
        ProductImage productImageB = new ProductImage(2, "/uploads/test_image_2.jpeg", true);
        productB.getImages().addAll(List.of(productImageA, productImageB));

        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));
        when(productImageRepository.findById(productImageA.getId())).thenReturn(Optional.of(productImageA));

        assertThatThrownBy(() -> productService.displayImage(productB.getId(), productImageA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("The image is related to a different product");
    }

    @Test
    public void shouldNotDisplayImageWhenSellerIsNotLoggedIn() {
        ProductImage productImageA = new ProductImage(1, "/uploads/test_image_1.jpeg", false);
        ProductImage productImageB = new ProductImage(2, "/uploads/test_image_2.jpeg", true);
        productA.getImages().addAll(List.of(productImageA, productImageB));
        authentication = new UsernamePasswordAuthenticationToken(
                productB.getSeller(),
                productB.getSeller().getPassword(),
                productB.getSeller().getAuthorities()
        );

        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));
        when(productImageRepository.findById(productImageA.getId())).thenReturn(Optional.of(productImageA));

        assertThatThrownBy(() -> productService.displayImage(productA.getId(), productImageA.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to manage images for this product");
    }

    @Test
    public void shouldDeleteProduct() throws IOException {
        ProductImage productImageA = new ProductImage(1, "/uploads/test_image_1.jpeg", false);
        productA.getImages().add(productImageA);

        when(productRepository.findWithImagesById(productA.getId())).thenReturn(Optional.of(productA));

        productService.delete(productA.getId());

        verify(productRepository, times(1)).deleteById(productA.getId());
        verify(fileService, times(1)).deleteFile(productImageA.getFilePath());
    }
}
