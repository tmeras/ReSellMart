package com.tmeras.resellmart.product;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

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
    private ProductRequest productRequestB;
    private ProductResponse productResponseA;
    private ProductResponse productResponseB;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Initialise test objects
        Category category = TestDataUtils.createCategoryA();
        Role adminRole = new Role(1, "ADMIN");
        Role userRole = new Role(2, "USER");
        User userA = TestDataUtils.createUserA(Set.of(adminRole));
        User userB = TestDataUtils.createUserB(Set.of(userRole));
        productA = TestDataUtils.createProductA(category, userA);
        productB = TestDataUtils.createProductB(category, userB);

        productRequestA = TestDataUtils.createProductRequestA(category.getId());
        productRequestB = TestDataUtils.createProductRequestB(category.getId());

        CategoryResponse categoryResponse = TestDataUtils.createCategoryResponseA();
        UserResponse userResponseA = TestDataUtils.createUserResponseA(Set.of(adminRole));
        UserResponse userResponseB = TestDataUtils.createUserResponseB(Set.of(userRole));
        productResponseA = TestDataUtils.createProductResponseA(categoryResponse, userResponseA);
        productResponseB = TestDataUtils.createProductResponseB(categoryResponse, userResponseB);

        authentication = new UsernamePasswordAuthenticationToken(
                userA, userA.getPassword(), userA.getAuthorities()
        );
    }

    @Test
    public void shouldSaveProductWhenValidRequest() {
        when(categoryRepository.findById(productRequestA.getCategoryId()))
                .thenReturn(Optional.of(productA.getCategory()));
        when(productMapper.toProduct(productRequestA)).thenReturn(productA);
        when(productRepository.save(productA)).thenReturn(productA);
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        ProductResponse productResponse = productService.save(productRequestA, authentication);

        assertThat(productResponse).isEqualTo(productResponseA);
    }

    @Test
    public void shouldNotSaveProductWhenInvalidCategoryId() {
        when(categoryRepository.findById(productRequestA.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.save(productRequestA, authentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void shouldNotSaveProductWhenInvalidPrice() {
        productRequestA.setPrice(productRequestA.getDiscountedPrice() - 1);
        when(categoryRepository.findById(productRequestA.getCategoryId()))
                .thenReturn(Optional.of(productA.getCategory()));

        assertThatThrownBy(() -> productService.save(productRequestA, authentication));
    }

    @Test
    public void shouldFindProductByIdWhenValidProductId() {
        when(productRepository.findById(productRequestA.getId())).thenReturn(Optional.of(productA));
        when(productMapper.toProductResponse(productA)).thenReturn(productResponseA);

        ProductResponse productResponse = productService.findById(productRequestA.getId());

        assertThat(productResponse).isEqualTo(productResponseA);
    }

    @Test
    public void shouldNotFindProductByIdWhenInvalidProductId() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99))
                .isInstanceOf(ResourceNotFoundException.class);
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

        when(productRepository.findAllByKeyword(pageable, "Test product", productA.getSeller().getId())).thenReturn(page);
        when(productMapper.toProductResponse(productB)).thenReturn(productResponseB);

        PageResponse<ProductResponse> pageResponse =
                productService.findAllByKeyword(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR, "Test product", authentication);

        assertThat(pageResponse.getContent().size()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(productResponseB);
    }


    
}
