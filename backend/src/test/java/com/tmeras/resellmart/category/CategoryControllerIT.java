package com.tmeras.resellmart.category;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.token.JwtService;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CategoryControllerIT {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final TestRestTemplate restTemplate;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CategoryRepository categoryRepository;

    private Category parentCategory;
    private Category childCategory;

    // TODO: Add category request fields

    // Used to add include JWT in requests
    private HttpHeaders headers;

    @Autowired
    public CategoryControllerIT(
            TestRestTemplate restTemplate, CategoryRepository categoryRepository,
            UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService
    ) {
        this.restTemplate = restTemplate;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @BeforeEach
    public void setUp() {
        // Empty relevant database tables
        userRepository.deleteAll();
        roleRepository.deleteAll();
        categoryRepository.deleteAll();

        // Save required entities and an admin user (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        User user = TestDataUtils.createUserA(Set.of(adminRole));
        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);

        parentCategory = TestDataUtils.createCategoryA();
        parentCategory.setId(null);
        parentCategory = categoryRepository.save(parentCategory);

        childCategory = TestDataUtils.createCategoryB();
        childCategory.setId(null);
        childCategory.setParentCategory(parentCategory);
        childCategory = categoryRepository.save(childCategory);

        // Generate test JWT with admin user details to include in each authenticated request
        String testJwt = jwtService.generateAccessToken(new HashMap<>(), user);
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + testJwt);
    }

    @Test
    public void shouldSaveCategoryWhenValidCategory() {
        CategoryRequest categoryRequest = CategoryRequest.builder().name("New category").build();

        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories", HttpMethod.POST, new HttpEntity<>(categoryRequest, headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void shouldNotSaveCategoryWhenInvalidCategory() {
        CategoryRequest categoryRequest = CategoryRequest.builder().build();

        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories", HttpMethod.POST,
                        new HttpEntity<>(categoryRequest, headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldNotSaveCategoryWhenDuplicateCategoryName() {
        CategoryRequest categoryRequest = CategoryRequest.builder().name("Test category A").build();

        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories", HttpMethod.POST, new HttpEntity<>(categoryRequest, headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void shouldNotSaveCategoryWhenInvalidParentId() {
        CategoryRequest categoryRequest = CategoryRequest.builder().name("New category").parentId(99).build();

        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories", HttpMethod.POST, new HttpEntity<>(categoryRequest, headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldNotSaveCategoryWhenInvalidParent() {
        CategoryRequest categoryRequest = CategoryRequest.builder().name("New category").parentId(childCategory.getId()).build();

        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories", HttpMethod.POST, new HttpEntity<>(categoryRequest, headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);    }

    @Test
    public void shouldFindCategoryWhenValidCategoryId() {
        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories/" + parentCategory.getId(),
                        HttpMethod.GET, new HttpEntity<>(headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(parentCategory.getName());
    }

    @Test
    public void shouldNotFindCategoryWhenInvalidCategoryId() {
        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories/99", HttpMethod.GET,
                        new HttpEntity<>(headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFindAllCategories() {
        ResponseEntity<PageResponse<CategoryResponse>> response =
                restTemplate.exchange("/api/categories", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(2);
    }

    @Test
    public void shouldFindAllCategoriesByParentId() {
        ResponseEntity<PageResponse<CategoryResponse>> response =
                restTemplate.exchange("/api/categories/parents/" + parentCategory.getId(), HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(1);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo(childCategory.getName());
    }

    @Test
    public void shouldFindAllParentCategories() {
        ResponseEntity<List<CategoryResponse>> response =
                restTemplate.exchange("/api/categories/parents", HttpMethod.GET,
                        new HttpEntity<>(headers), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo(parentCategory.getName());
    }

    @Test
    public void shouldUpdateCategoryWhenValidCategoryId() {
        CategoryRequest categoryRequest = new CategoryRequest(1, "Updated category", null);

        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories/" + parentCategory.getId(), HttpMethod.PUT,
                        new HttpEntity<>(categoryRequest, headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(parentCategory.getId());
        assertThat(response.getBody().getName()).isEqualTo(categoryRequest.getName());
        assertThat(response.getBody().getParentId()).isNull();
    }

    @Test
    public void shouldNotUpdateCategoryWhenInvalidCategoryId() {
        CategoryRequest categoryRequest = new CategoryRequest(1, "Updated category", null);

        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories/99", HttpMethod.PUT,
                        new HttpEntity<>(categoryRequest, headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldDeleteCategory() {
        ResponseEntity<CategoryResponse> response =
                restTemplate.exchange("/api/categories/" + childCategory.getId(), HttpMethod.DELETE,
                        new HttpEntity<>(headers), CategoryResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}