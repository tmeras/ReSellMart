package com.tmeras.resellmart.category;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.ResourceAlreadyExistsException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTests {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category categoryA;
    private Category categoryB;
    private CategoryRequest categoryRequestA;
    private CategoryRequest categoryRequestB;
    private CategoryResponse categoryResponseA;
    private CategoryResponse categoryResponseB;

    @BeforeEach
    void setUp() {
        // Initialise test objects
        categoryA = TestDataUtils.createCategoryA();
        categoryB = TestDataUtils.createCategoryB();
        categoryB.setParentCategory(categoryA);

        categoryRequestA = TestDataUtils.createCategoryRequestA();
        categoryRequestB = TestDataUtils.createCategoryRequestB();
        categoryRequestB.setParentId(categoryRequestA.getId());

        categoryResponseA = TestDataUtils.createCategoryResponseA();
        categoryResponseB = TestDataUtils.createCategoryResponseB();
        categoryResponseB.setParentId(categoryResponseA.getId());
    }

    @Test
    public void shouldSaveCategoryWhenValidRequest() {
        when(categoryRepository.findByName(categoryRequestA.getName())).thenReturn(Optional.empty());
        when(categoryMapper.toCategory(categoryRequestA)).thenReturn(categoryA);
        when(categoryRepository.save(categoryA)).thenReturn(categoryA);
        when(categoryMapper.toCategoryResponse(categoryA)).thenReturn(categoryResponseA);

        CategoryResponse categoryResponse = categoryService.save(categoryRequestA);

        assertThat(categoryResponse).isEqualTo(categoryResponseA);
    }

    @Test
    public void shouldNotSaveCategoryWhenDuplicateCategoryName() {
        when(categoryRepository.findByName(categoryRequestA.getName())).thenReturn(Optional.of(categoryA));

        assertThatThrownBy(() -> categoryService.save(categoryRequestA))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("A category with the name: '" + categoryRequestA.getName() + "' already exists");
    }

    @Test
    public void shouldNotSaveCategoryWhenInvalidParentId() {
        categoryRequestB.setParentId(99);
        when(categoryRepository.findByName(categoryRequestB.getName())).thenReturn(Optional.empty());
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.save(categoryRequestB))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No parent category found with ID: 99");
    }

    @Test
    public void shouldNotSaveCategoryWhenInvalidParent() {
        Category categoryC = new Category(3, "Test category A", null);
        categoryA.setParentCategory(categoryC);

        when(categoryRepository.findByName(categoryRequestB.getName())).thenReturn(Optional.empty());
        when(categoryRepository.findById(categoryRequestA.getId())).thenReturn(Optional.of(categoryA));

        assertThatThrownBy(() -> categoryService.save(categoryRequestB))
                .isInstanceOf(APIException.class)
                .hasMessage("Parent category should not have a parent");
    }

    @Test
    public void shouldFindCategoryByIdWhenValidCategoryId() {
        when(categoryRepository.findById(categoryA.getId())).thenReturn(Optional.of(categoryA));
        when(categoryMapper.toCategoryResponse(categoryA)).thenReturn(categoryResponseA);

        CategoryResponse categoryResponse = categoryService.findById(categoryA.getId());

        assertThat(categoryResponse).isEqualTo(categoryResponseA);
    }

    @Test
    public void shouldNotFindCategoryByIdWhenInvalidCategoryId() {
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No category found with ID: 99");
    }

    @Test
    public void shouldFindAllCategories() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_CATEGORIES_BY).ascending() : Sort.by(AppConstants.SORT_CATEGORIES_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Category> page = new PageImpl<>(List.of(categoryA, categoryB));

        when(categoryRepository.findAll(pageable)).thenReturn(page);
        when(categoryMapper.toCategoryResponse(categoryA)).thenReturn(categoryResponseA);
        when(categoryMapper.toCategoryResponse(categoryB)).thenReturn(categoryResponseB);

        PageResponse<CategoryResponse> pageResponse =
                categoryService.findAll(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_CATEGORIES_BY, AppConstants.SORT_DIR);

        assertThat(pageResponse.getContent().size()).isEqualTo(2);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(categoryResponseA);
        assertThat(pageResponse.getContent().get(1)).isEqualTo(categoryResponseB);
    }

    @Test
    public void shouldFindAllCategoriesByParentId() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_CATEGORIES_BY).ascending() : Sort.by(AppConstants.SORT_CATEGORIES_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Category> page = new PageImpl<>(List.of(categoryB));

        when(categoryRepository.findAllByParentId(pageable, categoryB.getParentCategory().getId())).thenReturn(page);
        when(categoryMapper.toCategoryResponse(categoryB)).thenReturn(categoryResponseB);

        PageResponse<CategoryResponse> pageResponse =
                categoryService.findAllByParentId(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_CATEGORIES_BY, AppConstants.SORT_DIR, categoryB.getParentCategory().getId());

        assertThat(pageResponse.getContent().size()).isEqualTo(1);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(categoryResponseB);
    }

    @Test
    public void shouldFindAllParentCategories() {
        when(categoryRepository.findAllParents()).thenReturn(List.of(categoryA));
        when(categoryMapper.toCategoryResponse(categoryA)).thenReturn(categoryResponseA);

        List<CategoryResponse> categoryResponses = categoryService.findAllParents();

        assertThat(categoryResponses.size()).isEqualTo(1);
        assertThat(categoryResponses.get(0)).isEqualTo(categoryResponseA);
    }

    @Test
    public void shouldUpdateCategoryWhenValidRequest() {
        categoryRequestA.setName("Updated test category A");
        categoryResponseA.setName("Updated test category A");

        when(categoryRepository.findById(categoryA.getId())).thenReturn(Optional.of(categoryA));
        when(categoryRepository.save(categoryA)).thenReturn(categoryA);
        when(categoryMapper.toCategoryResponse(categoryA)).thenReturn(categoryResponseA);

        CategoryResponse categoryResponse = categoryService.update(categoryRequestA, categoryA.getId());

        assertThat(categoryResponse).isEqualTo(categoryResponseA);
        assertThat(categoryA.getName()).isEqualTo("Updated test category A");
    }

    @Test
    public void shouldNotUpdateCategoryWhenInvalidCategoryId() {
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(categoryRequestA, 99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No category found with ID: 99");
    }

    @Test
    public void shouldDeleteCategory() {
        categoryService.delete(categoryA.getId());

        verify(categoryRepository, times(1)).deleteById(categoryA.getId());
    }
}
