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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TestDataUtils {

    private static final ZonedDateTime CURRENT_TIME = ZonedDateTime.now();

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
                .isEnabled(true)
                .isMfaEnabled(false)
                .roles(roles)
                .build();
    }

    public static UserRequest createUserRequestA() {
        return UserRequest.builder()
                .name("Test User A")
                .homeCountry("UK")
                .isMfaEnabled(false)
                .build();
    }

    public static UserResponse createUserResponseA(Set<Role> roles) {
        return UserResponse.builder()
                .id(1)
                .name("Test user A")
                .email("testA@test.com")
                .homeCountry("UK")
                .registeredAt(LocalDate.now())
                .isMfaEnabled(false)
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
                .isEnabled(true)
                .isMfaEnabled(false)
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
                .isMfaEnabled(false)
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
                .price(BigDecimal.valueOf(10.00))
                .previousPrice(BigDecimal.valueOf(5.0))
                .condition(ProductCondition.NEW)
                .availableQuantity(2)
                .listedAt(CURRENT_TIME)
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
                .price(BigDecimal.valueOf(10.00))
                .condition(ProductCondition.NEW)
                .availableQuantity(2)
                .categoryId(categoryId)
                .build();
    }

    public static ProductResponse createProductResponseA(CategoryResponse categoryResponse, UserResponse userResponse) {
        return ProductResponse.builder()
                .id(1)
                .name("Test product A")
                .description("Description A")
                .price(BigDecimal.valueOf(10.00))
                .previousPrice(BigDecimal.valueOf(5.0))
                .condition(ProductCondition.NEW)
                .availableQuantity(2)
                .listedAt(CURRENT_TIME)
                .isDeleted(false)
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
                .price(BigDecimal.valueOf(20.0))
                .previousPrice(BigDecimal.valueOf(10.0))
                .condition(ProductCondition.LIKE_NEW)
                .availableQuantity(3)
                .listedAt(CURRENT_TIME)
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
                .price(BigDecimal.valueOf(20.0))
                .previousPrice(BigDecimal.valueOf(10.0))
                .condition(ProductCondition.LIKE_NEW)
                .availableQuantity(3)
                .listedAt(CURRENT_TIME)
                .isDeleted(false)
                .category(categoryResponse)
                .seller(userResponse)
                .images(new ArrayList<>())
                .build();
    }

    public static Address createAddressA(User user) {
        return Address.builder()
                .id(1)
                .name("Test address A")
                .country("UK")
                .street("Mappin Street")
                .state("South Yorkshire")
                .city("Sheffield")
                .postalCode("S1 4DT")
                .phoneNumber("+441234567890")
                .isMain(false)
                .addressType(AddressType.HOME)
                .user(user)
                .build();
    }

    public static AddressRequest createAddressRequestA() {
        return AddressRequest.builder()
                .name("Test address A")
                .country("UK")
                .street("Mappin Street")
                .state("South Yorkshire")
                .city("Sheffield")
                .postalCode("S1 4DT")
                .phoneNumber("+441234567890")
                .isMain(false)
                .addressType("HOME")
                .build();
    }

    public static AddressResponse createAddressResponseA(Integer userId) {
        return AddressResponse.builder()
                .id(1)
                .name("Test address A")
                .country("UK")
                .street("Mappin Street")
                .state("South Yorkshire")
                .city("Sheffield")
                .postalCode("S1 4DT")
                .phoneNumber("+441234567890")
                .isMain(false)
                .addressType(AddressType.HOME)
                .userId(userId)
                .build();
    }

    public static Address createAddressB(User user) {
        return Address.builder()
                .id(2)
                .name("Test address B")
                .country("Greece")
                .street("Ermou Street")
                .state("Attica")
                .city("Athens")
                .postalCode("10563")
                .phoneNumber("+302112345678")
                .isMain(false)
                .addressType(AddressType.WORK)
                .user(user)
                .build();
    }

    public static AddressResponse createAddressResponseB(Integer userId) {
        return AddressResponse.builder()
                .id(2)
                .name("Test address B")
                .country("Greece")
                .street("Ermou Street")
                .state("Attica")
                .city("Athens")
                .postalCode("10563")
                .phoneNumber("+302112345678")
                .isMain(false)
                .addressType(AddressType.HOME)
                .userId(userId)
                .build();
    }

    public static Order createOrderA(User buyer, Address address, Product orderProduct) {
        OrderItem orderItem = OrderItem.builder()
                .id(1)
                .status(OrderItemStatus.PENDING_SHIPMENT)
                .product(orderProduct)
                .productQuantity(orderProduct.getAvailableQuantity())
                .productName(orderProduct.getName())
                .productPrice(orderProduct.getPrice())
                .productCondition(orderProduct.getCondition())
                .productSeller(orderProduct.getSeller())
                .build();

        return Order.builder()
                .id(1)
                .placedAt(CURRENT_TIME)
                .paymentMethod("cash")
                .status(OrderStatus.PAID)
                .stripeCheckoutId("stripe-id")
                .billingAddress(address.getFullAddress())
                .deliveryAddress(address.getFullAddress())
                .buyer(buyer)
                .orderItems(List.of(orderItem))
                .build();
    }

    public static OrderRequest createOrderRequestA(Integer addressId) {
        return OrderRequest.builder()
                .billingAddressId(addressId)
                .deliveryAddressId(addressId)
                .build();
    }

    public static OrderResponse createOrderResponseA(
            AddressResponse addressResponse, UserResponse userResponse, ProductResponse orderProduct
    ) {
        OrderItemResponse orderItemResponse = OrderItemResponse.builder()
                .id(1)
                .status(OrderItemStatus.PENDING_SHIPMENT)
                .productId(orderProduct.getId())
                .productQuantity(orderProduct.getAvailableQuantity())
                .productName(orderProduct.getName())
                .productPrice(orderProduct.getPrice())
                .productCondition(orderProduct.getCondition())
                .productSeller(orderProduct.getSeller())
                .build();

        return OrderResponse.builder()
                .id(1)
                .placedAt(CURRENT_TIME)
                .paymentMethod("cash")
                .status(OrderStatus.PAID)
                .stripeCheckoutId("stripe-id")
                .billingAddress(addressResponse.getFullAddress())
                .deliveryAddress(addressResponse.getFullAddress())
                .total(orderProduct.getPrice()
                        .multiply(BigDecimal.valueOf(orderProduct.getAvailableQuantity())))
                .buyer(userResponse)
                .orderItems(List.of(orderItemResponse))
                .build();
    }

    public static Order createOrderB(User buyer, Address address, Product orderProduct) {
        OrderItem orderItem = OrderItem.builder()
                .id(2)
                .status(OrderItemStatus.PENDING_SHIPMENT)
                .product(orderProduct)
                .productQuantity(orderProduct.getAvailableQuantity())
                .productName(orderProduct.getName())
                .productPrice(orderProduct.getPrice())
                .productCondition(orderProduct.getCondition())
                .productSeller(orderProduct.getSeller())
                .build();

        return Order.builder()
                .id(2)
                .placedAt(CURRENT_TIME)
                .paymentMethod("cash")
                .status(OrderStatus.PAID)
                .stripeCheckoutId("stripe-id-2")
                .billingAddress(address.getFullAddress())
                .deliveryAddress(address.getFullAddress())
                .buyer(buyer)
                .orderItems(List.of(orderItem))
                .build();
    }

    public static OrderResponse createOrderResponseB(
            AddressResponse addressResponse, UserResponse userResponse, ProductResponse orderProduct
    ) {
        OrderItemResponse orderItemResponse = OrderItemResponse.builder()
                .id(2)
                .status(OrderItemStatus.PENDING_SHIPMENT)
                .productId(orderProduct.getId())
                .productQuantity(orderProduct.getAvailableQuantity())
                .productName(orderProduct.getName())
                .productPrice(orderProduct.getPrice())
                .productCondition(orderProduct.getCondition())
                .productSeller(orderProduct.getSeller())
                .build();

        return OrderResponse.builder()
                .id(2)
                .placedAt(CURRENT_TIME)
                .paymentMethod("cash")
                .status(OrderStatus.PAID)
                .stripeCheckoutId("stripe-id-2")
                .billingAddress(addressResponse.getFullAddress())
                .deliveryAddress(addressResponse.getFullAddress())
                .total(orderProduct.getPrice()
                        .multiply(BigDecimal.valueOf(orderProduct.getAvailableQuantity())))
                .buyer(userResponse)
                .orderItems(List.of(orderItemResponse))
                .build();
    }
}
