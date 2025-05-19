package com.tmeras.resellmart.common;

import java.util.Set;

public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }

    // Product Constants
    public static final int MAX_PRODUCT_NAME_LENGTH = 100;
    public static final int MAX_PRODUCT_DESCRIPTION_LENGTH = 5000;
    public static final int MAX_PRODUCT_QUANTITY = 1000;
    public static final int MAX_PRODUCT_PRICE = 10000;

    // Pagination/Sorting constants
    public static final String PAGE_NUMBER = "0";
    public static final Integer PAGE_NUMBER_INT = 0;
    public static final String PAGE_SIZE = "10";
    public static final Integer PAGE_SIZE_INT = 10;
    public static final String SORT_DIR = "asc";
    public static final String SORT_CATEGORIES_BY = "id";
    public static final String SORT_PRODUCTS_BY = "id";
    public static final String SORT_ORDERS_BY = "id";
    public static final String SORT_USERS_BY = "id";
    public static final String SORT_ADDRESSES_BY = "id";

    // Image upload constants
    public static final Integer MAX_IMAGE_NUMBER = 5;
    public static final Set<String> ACCEPTED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    // Thymeleaf template names
    public static final String USER_ACTIVATION_TEMPLATE = "account_activation";
    public static final String PURCHASE_CONFIRMATION_TEMPLATE = "purchase_confirmation";
    public static final String SALE_CONFIRMATION_TEMPLATE = "sale_confirmation";
    public static final String ORDER_CANCELLATION_TEMPLATE = "order_cancellation";

    // Number of entities manually created using flyway
    // used to prevent deletion of related images       TODO: Update when more data is added
    public static final Integer FLYWAY_PRODUCTS_NUMBER = 17;
    public static final Integer FLYWAY_USERS_NUMBER = 3;
    public static final Integer FLYWAY_ORDERS_NUMBER = 5;
}
