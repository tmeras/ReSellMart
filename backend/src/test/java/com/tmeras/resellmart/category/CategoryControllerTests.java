package com.tmeras.resellmart.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.configuration.TestConfig;
import com.tmeras.resellmart.token.JwtFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
)
 //@AutoConfigureMockMvc(addFilters = false)
@Import(TestConfig.class)
@WithMockUser(roles = "ADMIN")
public class CategoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private CategoryRequest parentCategoryRequest;
    private CategoryRequest childCategoryRequest;
    private CategoryResponse parentCategoryResponse;
    private CategoryResponse childCategoryResponse;

    @BeforeEach
    public void setUp() {
        parentCategoryRequest = TestDataUtils.createCategoryRequestA();
        childCategoryRequest = TestDataUtils.createCategoryRequestB();
        childCategoryRequest.setParentId(parentCategoryRequest.getId());

        parentCategoryResponse = TestDataUtils.createCategoryResponseA();
        childCategoryResponse = TestDataUtils.createCategoryResponseB();
        childCategoryResponse.setParentId(parentCategoryResponse.getId());

        /*User testUser = TestDataUtils.createUserA(Set.of(new Role(1,"ADMIN")));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUser, null, testUser.getAuthorities()
        );

        // Set the authentication into SecurityContext
        SecurityContextHolder.getContext().setAuthentication(auth);*/
    }

    @Test
    public void shouldFindCategoryById() throws Exception {
        when(categoryService.findById(parentCategoryResponse.getId())).thenReturn(parentCategoryResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/categories/" + parentCategoryResponse.getId()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(parentCategoryResponse));
    }

    @Test
    public void shouldFindAllCategories() throws Exception {
        PageResponse<CategoryResponse> pageResponse = new PageResponse<>(
                List.of(parentCategoryResponse, childCategoryResponse),
                0, 5,
                2, 1,
                true, true
        );
        when(categoryService.findAll(
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                AppConstants.SORT_CATEGORIES_BY, AppConstants.SORT_DIR)
        ).thenReturn(pageResponse);

        MvcResult  mvcResult = mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }
}
