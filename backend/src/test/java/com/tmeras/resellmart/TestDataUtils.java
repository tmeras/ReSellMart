package com.tmeras.resellmart;

import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryMapper;
import com.tmeras.resellmart.category.CategoryRequest;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.user.User;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class TestDataUtils {

    private static CategoryMapper categoryMapper;

    private TestDataUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static CategoryRequest createCategoryRequestA() {
        return CategoryRequest.builder()
                .id(1)
                .name("Test category A")
                .build();
    }

    public static Category createCategoryA() {
        return Category.builder()
                .id(1)
                .name("Test category A")
                .build();
    }

    public static CategoryResponse createCategoryResponseA() {
        return CategoryResponse.builder()
                .id(1)
                .name("Test category A")
                .build();
    }


    public static CategoryRequest createCategoryRequestB() {
        return CategoryRequest.builder()
                .id(2)
                .name("Test category B")
                .build();
    }

    public static Category createCategoryB() {
        return Category.builder()
                .id(2)
                .name("Test category B")
                .build();
    }

    public static CategoryResponse createCategoryResponseB() {
        return CategoryResponse.builder()
                .id(2)
                .name("Test category B")
                .build();
    }

    public static User createUserA(Set<Role> roles) {
        return User.builder()
                .id(1)
                .name("Test user A")
                .password("password")
                .email("testA@test.com")
                .enabled(true)
                .roles(roles)
                .build();
    }

}
