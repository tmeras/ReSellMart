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
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
)
@Import(TestConfig.class)
@WithMockUser(roles = "ADMIN")
public class ProductControllerTests {

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
    void setUp() {
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

        mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productRequestA))
        ).andExpect(status().isBadRequest());
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
    public void shouldFindAllProductsBySellerId() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseA),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                2, 1,
                true, true
        );
        when(productService.findAllBySellerId(
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                AppConstants.SORT_PRODUCTS_BY, AppConstants.SORT_DIR,
                productResponseA.getSeller().getId()
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/user/" + productResponseA.getSeller().getId()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldFindAllProductsByCategoryId() throws Exception {
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(productResponseA, productResponseB),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                2, 1,
                true, true
        );
        when(productService.findAllByCategoryId(
                eq(AppConstants.PAGE_NUMBER_INT), eq(AppConstants.PAGE_SIZE_INT),
                eq(AppConstants.SORT_PRODUCTS_BY), eq(AppConstants.SORT_DIR),
                eq(productRequestA.getCategoryId()), any(Authentication.class)
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/category/" + productRequestA.getCategoryId()))
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
                eq(AppConstants.PAGE_NUMBER_INT), eq(AppConstants.PAGE_SIZE_INT),
                eq(AppConstants.SORT_PRODUCTS_BY), eq(AppConstants.SORT_DIR),
                eq("Test product"), any(Authentication.class)
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/search?keyword=Test product"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }




}
