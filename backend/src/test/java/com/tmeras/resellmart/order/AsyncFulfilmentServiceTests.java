package com.tmeras.resellmart.order;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.cart.CartItemRepository;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.email.EmailService;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.user.User;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AsyncFulfilmentServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AsyncFulfilmentService asyncFulfilmentService;

    private User userA;
    private Order orderA;

    @BeforeEach
    public void setUp() {
        // Initialise test objects
        Role adminRole = new Role(1, "ADMIN");
        Role userRole = new Role(2, "USER");
        userA = TestDataUtils.createUserA(Set.of(adminRole));
        User userB = TestDataUtils.createUserB(Set.of(userRole));
        Category category = TestDataUtils.createCategoryA();
        Product productB = TestDataUtils.createProductB(category, userB);

        Address addressA = TestDataUtils.createAddressA(userA);
        orderA = TestDataUtils.createOrderA(userA, addressA, productB);
    }

    @Test
    public void shouldFinaliseOrder() throws MessagingException {
        asyncFulfilmentService.finaliseOrder(orderA, List.of(orderA.getOrderItems().get(0).getProduct()));

        verify(orderRepository, times(1)).save(orderA);
        verify(productRepository, times(1)).saveAll(List.of(orderA.getOrderItems().get(0).getProduct()));
        verify(cartItemRepository, times(1))
                .deleteAllByUserIdAndProductIdIn(
                        userA.getId(),
                        List.of(orderA.getOrderItems().get(0).getProduct().getId()));
        verify(emailService, times(1))
                .sendPurchaseConfirmationEmail(orderA.getBuyer().getEmail(), orderA);
        verify(emailService, times(1))
                .sendSaleConfirmationEmail(
                        orderA.getOrderItems().get(0).getProductSeller().getEmail(),
                        orderA.getOrderItems().get(0).getProductSeller().getRealName(),
                        orderA,
                        orderA.getOrderItems(),
                        orderA.calculateTotalPrice());
    }
}
