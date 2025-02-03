package com.tmeras.resellmart.category;

import com.tmeras.resellmart.TestDataUtils;
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
        categoryRequestA = TestDataUtils.createCategoryRequestA();
        categoryRequestB = TestDataUtils.createCategoryRequestB();
        categoryResponseA = TestDataUtils.createCategoryResponseA();
        categoryResponseB = TestDataUtils.createCategoryResponseB();
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
