package com.tmeras.resellmart.order;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.address.AddressRepository;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryRepository;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.role.RoleRepository;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class OrderRepositoryTests {

    @Container
    @ServiceConnection
    private static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.2.0");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    private Order orderA1;
    private Order orderA2;
    private Order orderB1;
    private Order orderB2;

    @Autowired
    public OrderRepositoryTests(
            UserRepository userRepository, RoleRepository roleRepository,
            AddressRepository addressRepository, ProductRepository productRepository,
            CategoryRepository categoryRepository, OrderRepository orderRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    @BeforeEach
    public void setUp() {
        // Save required entities (need to set IDs to null before inserting to avoid
        // errors related to MySQL's AUTO_INCREMENT counter not resetting between tests)
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        User userA = TestDataUtils.createUserA(Set.of(adminRole));
        userA.setId(null);
        userA = userRepository.save(userA);

        Role userRole = roleRepository.save(Role.builder().name("USER").build());
        User userB = TestDataUtils.createUserB(Set.of(userRole));
        userB.setId(null);
        userB = userRepository.save(userB);

        Category category = categoryRepository.save(Category.builder().name("category").build());
        Product productA = TestDataUtils.createProductA(category, userA);
        productA.setId(null);
        productA = productRepository.save(productA);
        Product productB = TestDataUtils.createProductB(category, userB);
        productB.setId(null);
        productB = productRepository.save(productB);

        Address addressA = TestDataUtils.createAddressA(userA);
        addressA.setId(null);
        addressA = addressRepository.save(addressA);
        Address addressB = TestDataUtils.createAddressB(userB);
        addressB.setId(null);
        addressB = addressRepository.save(addressB);

        // Save one paid and one unpaid order for each user
        orderA1 = TestDataUtils.createOrderA(userA, addressA, productB);
        orderA1.setId(null);
        orderA1.getOrderItems().get(0).setId(null);
        orderA1 = orderRepository.save(orderA1);
        orderA2 = TestDataUtils.createOrderA(userA, addressA, productB);
        orderA2.setId(null);
        orderA2.setStatus(OrderStatus.PENDING_PAYMENT);
        orderA2.getOrderItems().get(0).setId(null);
        orderA2 = orderRepository.save(orderA2);

        orderB1 = TestDataUtils.createOrderB(userB, addressB, productA);
        orderB1.setId(null);
        orderB1.getOrderItems().get(0).setId(null);
        orderB1 = orderRepository.save(orderB1);
        orderB2 = TestDataUtils.createOrderB(userB, addressB, productA);
        orderB2.setId(null);
        orderB2.setStatus(OrderStatus.PENDING_PAYMENT);
        orderB2.getOrderItems().get(0).setId(null);
        orderB2 = orderRepository.save(orderB2);
    }

    @Test
    public void shouldFindAllPaidOrdersByBuyerId() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_ORDERS_BY).ascending() : Sort.by(AppConstants.SORT_ORDERS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);

        Page<Order> page = orderRepository.findAllPaidByBuyerId(pageable, orderA1.getBuyer().getId());

        assertThat(page.getContent().size()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(orderA1.getId());
    }

    @Test
    public void shouldFindAllPaidOrdersByProductSellerId() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_ORDERS_BY).ascending() : Sort.by(AppConstants.SORT_ORDERS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);

        Page<Order> page =
                orderRepository.findAllPaidByProductSellerId(pageable, orderB1.getOrderItems().get(0).getProductSeller().getId());

        assertThat(page.getContent().size()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(orderB1.getId());
    }

    @Test
    public void shouldCalculateOrderStatistics() {
        Integer expectedProductSales = Stream.of(orderA1, orderB1)
                .flatMap(order -> order.getOrderItems().stream())
                .map(OrderItem::getProductQuantity)
                .reduce(0, Integer::sum);
        BigDecimal expectedRevenue = orderA1.calculateTotalPrice().add(orderB1.calculateTotalPrice());
        ZonedDateTime to = ZonedDateTime.now();
        ZonedDateTime from = to.minusMonths(1);

        OrderStatsResponse statistics = orderRepository.calculateStatistics(from, to);

        assertThat(statistics.getMonthlyOrderCount()).isEqualTo(2);
        assertThat(statistics.getMonthlyProductSales()).isEqualTo(expectedProductSales);
        assertThat(statistics.getMonthlyRevenue()).isEqualTo(expectedRevenue);
    }
}
