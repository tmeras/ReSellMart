package com.tmeras.resellmart.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.configuration.TestConfig;
import com.tmeras.resellmart.token.JwtFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private CategoryRequest categoryRequestA;
    private CategoryResponse categoryResponseA;
    private CategoryResponse categoryResponseB;

    @BeforeEach
    public void setUp() {
        // Initialise test objects
        categoryRequestA = TestDataUtils.createCategoryRequestA();
        categoryRequestA.setParentId(1);
        categoryResponseA = TestDataUtils.createCategoryResponseA();
        categoryResponseB = TestDataUtils.createCategoryResponseB();
        categoryResponseB.setParentId(categoryResponseA.getId());
    }

    @Test
    public void shouldSaveCategoryWhenValidRequest() throws Exception {


        when(categoryService.save(any(CategoryRequest.class))).thenReturn(categoryResponseA);

        MvcResult mvcResult = mockMvc.perform(post("/api/categories")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(categoryRequestA))
        ).andExpect(status().isCreated()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(categoryResponseA));
    }

    @Test
    public void shouldNotSaveCategoryWhenInvalidRequest() throws Exception {
        categoryRequestA.setName(null);
        categoryRequestA.setParentId(null);
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("name", "Name must not be empty");
        expectedErrors.put("parentId", "Parent category ID must not be empty");

        MvcResult mvcResult = mockMvc.perform(post("/api/categories")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(categoryRequestA))
        ).andExpect(status().isBadRequest()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(expectedErrors));
    }

    @Test
    public void shouldFindCategoryById() throws Exception {
        when(categoryService.findById(categoryResponseA.getId())).thenReturn(categoryResponseA);

        MvcResult mvcResult = mockMvc.perform(get("/api/categories/" + categoryResponseA.getId()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(categoryResponseA));
    }

    @Test
    public void shouldFindAllCategories() throws Exception {
        List<CategoryResponse> categoryResponses =
                List.of(categoryResponseA, categoryResponseB);

        when(categoryService.findAll(
                AppConstants.SORT_CATEGORIES_BY, AppConstants.SORT_DIR
        )).thenReturn(categoryResponses);

        MvcResult mvcResult = mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(categoryResponses));
    }

    @Test
    public void shouldFindAllCategoriesByKeyword() throws Exception {
        List<CategoryResponse> categoryResponses =
                List.of(categoryResponseA, categoryResponseB);

        when(categoryService.findAllByKeyword(
                AppConstants.SORT_CATEGORIES_BY, AppConstants.SORT_DIR, "test product"
        )).thenReturn(categoryResponses);

        MvcResult mvcResult = mockMvc.perform(get("/api/categories?search=test product"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(categoryResponses));
    }

    @Test
    public void shouldFindAllParentCategories() throws Exception {
        List<CategoryResponse> categoryResponses = List.of(categoryResponseA);
        when(categoryService.findAllParents()).thenReturn(categoryResponses);

        MvcResult mvcResult = mockMvc.perform(get("/api/categories/parents"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(categoryResponses));
    }

    @Test
    public void shouldUpdateCategory() throws Exception {
        categoryRequestA.setName("Updated category name");
        categoryResponseA.setName("Updated category name");

        when(categoryService.update(any(CategoryRequest.class), eq(categoryRequestA.getId()))).thenReturn(categoryResponseA);

        MvcResult mvcResult = mockMvc.perform(put("/api/categories/" + categoryRequestA.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(categoryRequestA))
        ).andExpect(status().isOk()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(categoryResponseA));
    }

    @Test
    public void shouldNotUpdateCategoryWhenInvalidRequest() throws Exception {
        categoryRequestA.setName(null);
        categoryRequestA.setParentId(null);
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("name", "Name must not be empty");
        expectedErrors.put("parentId", "Parent category ID must not be empty");

        MvcResult mvcResult = mockMvc.perform(put("/api/categories/" + categoryRequestA.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(categoryRequestA))
        ).andExpect(status().isBadRequest()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(expectedErrors));
    }

    @Test
    public void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/" + categoryRequestA.getId()))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).delete(categoryRequestA.getId());
    }
}
