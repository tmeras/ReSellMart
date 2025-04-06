package com.tmeras.resellmart.common;

public class AppConstants {

    // Pagination constants
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

    // Thymeleaf template names
    public static final String USER_ACTIVATION_TEMPLATE = "account_activation";
    public static final String ORDER_CONFIRMATION_TEMPLATE = "order_confirmation";

    // Number of entities manually created using flyway
    // used to prevent deletion of related images       TODO: Update when more data is added
    public static final Integer FLYWAY_PRODUCTS_NUMBER = 17;
}
