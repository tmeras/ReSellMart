package com.tmeras.resellmart.category;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.ResourceAlreadyExistsException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
    public void shouldSaveCategoryWhenValidCategory() {
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
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    public void shouldNotSaveCategoryWhenInvalidParentId() {
        when(categoryRepository.findByName(categoryRequestB.getName())).thenReturn(Optional.empty());
        when(categoryRepository.findById(categoryRequestA.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.save(categoryRequestB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void shouldNotSaveCategoryWhenInvalidParent() {
        Category categoryC = new Category(3, "Test category A", null);
        categoryA.setParentCategory(categoryC);

        // Evaluate that an expression is thrown if the parent category
        // of the category being saved also has a parent
        when(categoryRepository.findByName(categoryRequestB.getName())).thenReturn(Optional.empty());
        when(categoryRepository.findById(categoryRequestA.getId())).thenReturn(Optional.of(categoryA));

        assertThatThrownBy(() -> categoryService.save(categoryRequestB))
                .isInstanceOf(APIException.class)
                .withFailMessage("Parent category should not have a parent");
    }

    @Test
    public void shouldFindCategoryWhenValidCategoryId() {
        when(categoryRepository.findById(categoryA.getId())).thenReturn(Optional.of(categoryA));
        when(categoryMapper.toCategoryResponse(categoryA)).thenReturn(categoryResponseA);

        CategoryResponse categoryResponse = categoryService.findById(categoryA.getId());

        assertThat(categoryResponse).isEqualTo(categoryResponseA);
    }

    @Test
    public void shouldThrowNotFoundWhenInvalidCategoryId() {
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
