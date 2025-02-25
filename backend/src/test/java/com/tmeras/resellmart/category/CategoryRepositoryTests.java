package com.tmeras.resellmart.category;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.common.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class CategoryRepositoryTests {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final CategoryRepository categoryRepository;

    private Category parentCategory;
    private Category childCategory;

    @Autowired
    public CategoryRepositoryTests(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @BeforeEach
    public void setUp() {
        // Save required entities (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        parentCategory = TestDataUtils.createCategoryA();
        parentCategory.setId(null);
        parentCategory = categoryRepository.save(parentCategory);

        childCategory = TestDataUtils.createCategoryB();
        childCategory.setId(null);
        childCategory.setParentCategory(parentCategory);
        childCategory = categoryRepository.save(childCategory);
    }

    @Test
    public void shouldFindCategoryByName() {
        Optional<Category> category = categoryRepository.findByName(parentCategory.getName());

        assertThat(category.isPresent()).isTrue();
        assertThat(category.get().getId()).isEqualTo(parentCategory.getId());
        assertThat(category.get().getName()).isEqualTo(parentCategory.getName());
    }

    @Test
    public void shouldFindAllCategoriesByParentId() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_CATEGORIES_BY).ascending() : Sort.by(AppConstants.SORT_CATEGORIES_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);

        Page<Category> categoryPage = categoryRepository.findAllByParentId(pageable, parentCategory.getId());

        assertThat(categoryPage.getContent().size()).isEqualTo(1);
        assertThat(categoryPage.getContent().get(0).getId()).isEqualTo(childCategory.getId());
        assertThat(categoryPage.getContent().get(0).getName()).isEqualTo(childCategory.getName());
    }

    @Test
    public void shouldFindAllParentCategories() {
        List<Category> categories = categoryRepository.findAllParents();

        assertThat(categories.size()).isEqualTo(1);
        assertThat(categories.get(0).getId()).isEqualTo(parentCategory.getId());
        assertThat(categories.get(0).getName()).isEqualTo(parentCategory.getName());
    }
}
