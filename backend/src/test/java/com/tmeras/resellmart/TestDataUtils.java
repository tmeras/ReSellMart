package com.tmeras.resellmart;

import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.address.AddressRequest;
import com.tmeras.resellmart.address.AddressResponse;
import com.tmeras.resellmart.address.AddressType;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRequest;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.order.*;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductCondition;
import com.tmeras.resellmart.product.ProductRequest;
import com.tmeras.resellmart.product.ProductResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRequest;
import com.tmeras.resellmart.user.UserResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TestDataUtils {

    private static final LocalDateTime CURRENT_TIME = LocalDateTime.now();

    private TestDataUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static User createUserA(Set<Role> roles) {
        return User.builder()
                .id(1)
                .name("Test user A")
                .password("password")
                .email("testA@test.com")
                .homeCountry("UK")
                .registeredAt(LocalDate.now())
                .enabled(true)
                .roles(roles)
                .build();
    }

    public static UserRequest createUserRequestA() {
        return UserRequest.builder()
                .name("Test User A")
                .homeCountry("UK")
                .mfaEnabled(false)
                .build();
    }

    public static UserResponse createUserResponseA(Set<Role> roles) {
        return UserResponse.builder()
                .id(1)
                .name("Test user A")
                .email("testA@test.com")
                .homeCountry("UK")
                .registeredAt(LocalDate.now())
                .mfaEnabled(false)
                .roles(roles)
                .build();
    }

    public static User createUserB(Set<Role> roles) {
        return User.builder()
                .id(2)
                .name("Test user B")
                .password("password")
                .email("testB@test.com")
                .homeCountry("Greece")
                .registeredAt(LocalDate.now())
                .enabled(true)
                .roles(roles)
                .build();
    }

    public static UserResponse createUserResponseB(Set<Role> roles) {
        return UserResponse.builder()
                .id(2)
                .name("Test user B")
                .email("testB@test.com")
                .homeCountry("Greece")
                .registeredAt(LocalDate.now())
                .mfaEnabled(false)
                .roles(roles)
                .build();
    }

    public static Category createCategoryA() {
        return Category.builder()
                .id(1)
                .name("Test category A")
                .build();
    }

    public static CategoryRequest createCategoryRequestA() {
        return CategoryRequest.builder()
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

    public static Category createCategoryB() {
        return Category.builder()
                .id(2)
                .name("Test category B")
                .build();
    }

    public static CategoryRequest createCategoryRequestB() {
        return CategoryRequest.builder()
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

    public static Product createProductA(Category category, User seller) {
        return Product.builder()
                .id(1)
                .name("Test product A")
                .description("Description A")
                .price(10.0)
                .previousPrice(5.0)
                .productCondition(ProductCondition.NEW)
                .availableQuantity(2)
                .isDeleted(false)
                .category(category)
                .seller(seller)
                .images(new ArrayList<>())
                .build();
    }

    public static ProductRequest createProductRequestA(Integer categoryId) {
        return ProductRequest.builder()
                .id(1)
                .name("Test product A")
                .description("Description A")
                .price(10.0)
                .productCondition(ProductCondition.NEW)
                .availableQuantity(2)
                .categoryId(categoryId)
                .build();
    }

    public static ProductResponse createProductResponseA(CategoryResponse categoryResponse, UserResponse userResponse) {
        return ProductResponse.builder()
                .id(1)
                .name("Test product A")
                .description("Description A")
                .price(10.0)
                .previousPrice(5.0)
                .productCondition(ProductCondition.NEW)
                .availableQuantity(2)
                .deleted(false)
                .category(categoryResponse)
                .seller(userResponse)
                .images(new ArrayList<>())
                .build();
    }

    public static Product createProductB(Category category, User seller) {
        return Product.builder()
                .id(1)
                .name("Test product B")
                .description("Description B")
                .price(20.0)
                .previousPrice(10.0)
                .productCondition(ProductCondition.LIKE_NEW)
                .availableQuantity(3)
                .isDeleted(false)
                .category(category)
                .seller(seller)
                .images(new ArrayList<>())
                .build();
    }

    public static ProductResponse createProductResponseB(CategoryResponse categoryResponse, UserResponse userResponse) {
        return ProductResponse.builder()
                .id(1)
                .name("Test product B")
                .description("Description B")
                .price(20.0)
                .previousPrice(10.0)
                .productCondition(ProductCondition.LIKE_NEW)
                .availableQuantity(3)
                .deleted(false)
                .category(categoryResponse)
                .seller(userResponse)
                .images(new ArrayList<>())
                .build();
    }

    public static Address createAddressA(User user) {
        return Address.builder()
                .id(1)
                .country("UK")
                .street("Mappin Street")
                .state("South Yorkshire")
                .city("Sheffield")
                .postalCode("S1 4DT")
                .main(false)
                .deleted(false)
                .addressType(AddressType.HOME)
                .user(user)
                .build();
    }

    public static AddressRequest createAddressRequestA() {
        return AddressRequest.builder()
                .country("UK")
                .street("Mappin Street")
                .state("South Yorkshire")
                .city("Sheffield")
                .postalCode("S1 4DT")
                .main(false)
                .deleted(false)
                .addressType("HOME")
                .build();
    }

    public static AddressResponse createAddressResponseA(Integer userId) {
        return AddressResponse.builder()
                .id(1)
                .country("UK")
                .street("Mappin Street")
                .state("South Yorkshire")
                .city("Sheffield")
                .postalCode("S1 4DT")
                .main(false)
                .deleted(false)
                .addressType(AddressType.HOME)
                .userId(userId)
                .build();
    }

    public static Address createAddressB(User user) {
        return Address.builder()
                .id(2)
                .country("Greece")
                .street("Ermou Street")
                .state("Attica")
                .city("Athens")
                .postalCode("10563")
                .main(false)
                .deleted(false)
                .addressType(AddressType.WORK)
                .user(user)
                .build();
    }

    public static AddressResponse createAddressResponseB(Integer userId) {
        return AddressResponse.builder()
                .id(2)
                .country("Greece")
                .street("Ermou Street")
                .state("Attica")
                .city("Athens")
                .postalCode("10563")
                .main(false)
                .deleted(false)
                .addressType(AddressType.HOME)
                .userId(userId)
                .build();
    }

    public static Order createOrderA(User buyer, Address address, Product orderProduct) {
        OrderItem orderItem = OrderItem.builder()
                .id(1)
                .product(orderProduct)
                .productQuantity(1)
                .build();

        return Order.builder()
                .id(1)
                .placedAt(CURRENT_TIME)
                .billingAddress(address)
                .deliveryAddress(address)
                .paymentMethod(PaymentMethod.CASH)
                .buyer(buyer)
                .orderItems(List.of(orderItem))
                .build();
    }

    public static OrderRequest createOrderRequestA(Integer addressId) {
        return OrderRequest.builder()
                .paymentMethod(String.valueOf(PaymentMethod.CASH))
                .billingAddressId(addressId)
                .deliveryAddressId(addressId)
                .build();
    }

    public static OrderResponse createOrderResponseA(
            AddressResponse addressResponse, UserResponse userResponse, ProductResponse productResponse
    ) {
        OrderItemResponse orderItemResponse = OrderItemResponse.builder()
                .id(1)
                .product(productResponse)
                .productQuantity(1)
                .build();

        return OrderResponse.builder()
                .id(1)
                .placedAt(CURRENT_TIME)
                .paymentMethod(PaymentMethod.CASH)
                .deliveryAddress(addressResponse)
                .billingAddress(addressResponse)
                .buyer(userResponse)
                .orderItems(List.of(orderItemResponse))
                .build();
    }

    public static Order createOrderB(User buyer, Address address, Product orderProduct) {
        OrderItem orderItem = OrderItem.builder()
                .id(2)
                .product(orderProduct)
                .productQuantity(2)
                .build();

        return Order.builder()
                .id(2)
                .placedAt(CURRENT_TIME)
                .billingAddress(address)
                .deliveryAddress(address)
                .paymentMethod(PaymentMethod.CARD)
                .buyer(buyer)
                .orderItems(List.of(orderItem))
                .build();
    }

    public static OrderResponse createOrderResponseB(
            AddressResponse addressResponse, UserResponse userResponse, ProductResponse productResponse
    ) {
        OrderItemResponse orderItemResponse = OrderItemResponse.builder()
                .id(2)
                .product(productResponse)
                .productQuantity(2)
                .build();

        return OrderResponse.builder()
                .id(2)
                .placedAt(CURRENT_TIME)
                .paymentMethod(PaymentMethod.CARD)
                .deliveryAddress(addressResponse)
                .billingAddress(addressResponse)
                .buyer(userResponse)
                .orderItems(List.of(orderItemResponse))
                .build();
    }
}
