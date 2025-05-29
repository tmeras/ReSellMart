package com.tmeras.resellmart.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.configuration.TestConfig;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.token.JwtFilter;
import com.tmeras.resellmart.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
)
@Import(TestConfig.class)
@WithMockUser(roles = "ADMIN")
public class ProductControllerTests {

    public static final MockMultipartFile TEST_IMAGE_1;
    public static final MockMultipartFile TEST_IMAGE_2;

    static {
        try {
            TEST_IMAGE_1 = new MockMultipartFile(
                    "images", "test_image_1.jpeg", "image/jpeg",
                    Files.readAllBytes(Path.of("src/test/resources/test_image_1.jpeg"))
            );
            TEST_IMAGE_2 = new MockMultipartFile(
                    "images", "test_image_2.jpeg", "image/jpeg",
                    Files.readAllBytes(Path.of("src/test/resources/test_image_2.jpeg"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private ProductRequest productRequestA;
    private ProductResponse productResponseA;
    private ProductResponse productResponseB;

    @BeforeEach
    public void setUp() {
        //Initialise test objects
        UserResponse adminUserResponse = TestDataUtils.createUserResponseA(
                Set.of(new Role(1, "ADMIN"))
        );
        UserResponse userResponse = TestDataUtils.createUserResponseB(
                Set.of(new Role(2, "USER"))
        );
        CategoryResponse categoryResponse = TestDataUtils.createCategoryResponseA();

        productRequestA = TestDataUtils.createProductRequestA(1);
        productResponseA = TestDataUtils.createProductResponseA(categoryResponse, adminUserResponse);
        productResponseB = TestDataUtils.createProductResponseB(categoryResponse, userResponse);
    }

    @Test
    public void shouldSaveProductWhenValidRequest() throws Exception {
        when(productService.save(any(ProductRequest.class), any(Authentication.class))).thenReturn(productResponseA);

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productRequestA))
        ).andExpect(status().isCreated()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(productResponseA));
    }

    @Test
    public void shouldNotSaveProductWhenInvalidRequest() throws Exception {
        productRequestA.setName(null);
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("name", "Name must not be empty");

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productRequestA))
        ).andExpect(status().isBadRequest()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(expectedErrors));
    }

    @Test
    public void shouldFindProductById() throws Exception {
        when(productService.findById(productRequestA.getId())).thenReturn(productResponseA);

        MvcResult result = mockMvc.perform(get("/api/products/" + productRequestA.getId()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(productResponseA));
    }

    @Test
    public void shouldFindAllProducts() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseA, productResponseB),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                2, 1,
                true, true
        );

        when(productService.findAll(
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldFindAllProductsByKeyword() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseA, productResponseB),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                2, 1,
                true, true
        );

        when(productService.findAllByKeyword(
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR, "Test product"
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/products?search=Test product"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldFindAllProductsExceptSellerProducts() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseB),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                1, 1,
                true, true
        );

        when(productService.findAllExceptSellerProducts(
                eq(AppConstants.PAGE_NUMBER_INT), eq(AppConstants.PAGE_SIZE_INT),
                eq(AppConstants.SORT_PRODUCTS_BY), eq(AppConstants.SORT_DIR),
                any(Authentication.class)
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/others"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldFindAllProductsByKeywordExceptSellerProducts() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseB),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                1, 1,
                true, true
        );

        when(productService.findAllExceptSellerProductsByKeyword(
                eq(AppConstants.PAGE_NUMBER_INT), eq(AppConstants.PAGE_SIZE_INT),
                eq(AppConstants.SORT_PRODUCTS_BY), eq(AppConstants.SORT_DIR),
                eq("Test product"), any(Authentication.class)
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/others?search=Test product"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldFindAllProductsBySellerId() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseA),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                1, 1,
                true, true
        );

        when(productService.findAllBySellerId(
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR,
                productResponseA.getSeller().getId()
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/users/" + productResponseA.getSeller().getId()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldFindAllProductsBySellerIdAndKeyword() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseA),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                1, 1,
                true, true
        );

        when(productService.findAllBySellerIdAndKeyword(
                eq(AppConstants.PAGE_NUMBER_INT), eq(AppConstants.PAGE_SIZE_INT),
                eq(AppConstants.SORT_PRODUCTS_BY), eq(AppConstants.SORT_DIR),
                eq(productResponseA.getSeller().getId()), eq("Test product")
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(
                        get("/api/products/users/" + productResponseA.getSeller().getId() + "?search=Test product"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldFindAllProductsByCategoryId() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseB),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                1, 1,
                true, true
        );

        when(productService.findAllByCategoryId(
                eq(AppConstants.PAGE_NUMBER_INT), eq(AppConstants.PAGE_SIZE_INT),
                eq(AppConstants.SORT_PRODUCTS_BY), eq(AppConstants.SORT_DIR),
                eq(productRequestA.getCategoryId()), any(Authentication.class)
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/categories/" + productRequestA.getCategoryId()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldFindAllProductsByCategoryIdAndKeyword() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseB),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                1, 1,
                true, true
        );

        when(productService.findAllByCategoryIdAndKeyword(
                eq(AppConstants.PAGE_NUMBER_INT), eq(AppConstants.PAGE_SIZE_INT),
                eq(AppConstants.SORT_PRODUCTS_BY), eq(AppConstants.SORT_DIR),
                eq(productRequestA.getCategoryId()), eq("Test product"), any(Authentication.class)
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(
                        get("/api/products/categories/" + productRequestA.getCategoryId() + "?search=Test product"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldUpdateProductWhenValidRequest() throws Exception {
        ProductUpdateRequest productUpdateRequest = ProductUpdateRequest.builder()
                .name("Updated test product A")
                .description("Updated description A")
                .price(BigDecimal.valueOf(10.0))
                .productCondition(ProductCondition.NEW)
                .availableQuantity(2)
                .categoryId(productRequestA.getCategoryId())
                .isDeleted(false)
                .build();
        productResponseA.setName("Updated test product A");
        productRequestA.setDescription("Updated description A");

        when(productService.update(
                any(ProductUpdateRequest.class), eq(productRequestA.getId()), any(Authentication.class))
        ).thenReturn(productResponseA);

        MvcResult mvcResult = mockMvc.perform(patch("/api/products/" + productRequestA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productUpdateRequest))
        ).andExpect(status().isOk()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(productResponseA));
    }

    @Test
    public void shouldUploadProductImages() throws Exception {
        productResponseA.setImages(List.of(
                new ProductImageResponse(
                        1,
                        TEST_IMAGE_1.getOriginalFilename(),
                        TEST_IMAGE_1.getContentType(),
                        TEST_IMAGE_1.getBytes()
                ),
                new ProductImageResponse(
                        2,
                        TEST_IMAGE_2.getOriginalFilename(),
                        TEST_IMAGE_2.getContentType(),
                        TEST_IMAGE_2.getBytes()
                )
        ));

        when(productService.uploadProductImages(anyList(), eq(productRequestA.getId()), any(Authentication.class)))
                .thenReturn(productResponseA);

        MvcResult result = mockMvc.perform(multipart("/api/products/" + productRequestA.getId() + "/images")
                .file(TEST_IMAGE_1)
                .file(TEST_IMAGE_2)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(request -> { request.setMethod("PUT"); return request; })
        ).andExpect(status().isOk()).andReturn();
        String jsonResponse = result.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(productResponseA));
    }

    @Test
    public void shouldFindPrimaryProductImage() throws Exception {
        ProductImageResponse productImageResponse = new ProductImageResponse(
                1,
                TEST_IMAGE_1.getOriginalFilename(),
                TEST_IMAGE_1.getContentType(),
                TEST_IMAGE_1.getBytes()
        );

        when(productService.findPrimaryProductImage(productRequestA.getId())).thenReturn(productImageResponse);

        MvcResult result = mockMvc.perform(get("/api/products/" + productRequestA.getId() + "/images/primary"))
                .andExpect(status().isOk())
                .andReturn();
        byte[] imageBytes = result.getResponse().getContentAsByteArray();

        assertThat(imageBytes).isEqualTo(TEST_IMAGE_1.getBytes());
    }

    @Test
    public void shouldUpdateProductAvailabilityWhenValidRequest() throws Exception {
        ProductAvailabilityRequest productAvailabilityRequest = new ProductAvailabilityRequest(true);

        mockMvc.perform(patch("/api/products/" + productRequestA.getId() + "/availability")
                .content(objectMapper.writeValueAsString(productAvailabilityRequest))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());

        verify(productService, times(1))
                .updateAvailability(productRequestA.getId(), productAvailabilityRequest.getIsDeleted());
    }

    @Test
    public void shouldNotUpdateProductAvailabilityWhenInvalidRequest() throws Exception {
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("isDeleted", "Deleted flag must not be empty");
        ProductAvailabilityRequest productAvailabilityRequest = new ProductAvailabilityRequest(null);

        MvcResult mvcResult = mockMvc.perform(patch("/api/products/" + productRequestA.getId() + "/availability")
                .content(objectMapper.writeValueAsString(productAvailabilityRequest))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(expectedErrors));
    }
}
