package com.tmeras.resellmart;

import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryMapper;
import com.tmeras.resellmart.category.CategoryRequest;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.user.User;

import java.util.Optional;
import java.util.Set;

public final class TestDataUtils {

    private static CategoryMapper categoryMapper;

    private TestDataUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static CategoryRequest createCategoryRequestA() {
        return CategoryRequest.builder()
                .name("Test category A")
                .build();
    }

    public static Category createCategoryA() {
        return Category.builder()
                .name("Test category A")
                .build();
    }

    public static CategoryResponse createCategoryResponseA() {
        return CategoryResponse.builder()
                .name("Test category A")
                .build();
    }


    public static CategoryRequest createCategoryRequestB() {
        return CategoryRequest.builder()
                .name("Test category B")
                .build();
    }

    public static Category createCategoryB() {
        return Category.builder()
                .name("Test category B")
                .build();
    }

    public static CategoryResponse createCategoryResponseB() {
        return CategoryResponse.builder()
                .name("Test category B")
                .build();
    }



}
