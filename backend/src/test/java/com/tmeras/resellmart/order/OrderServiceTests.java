package com.tmeras.resellmart.order;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.address.Address;
import com.tmeras.resellmart.address.AddressRepository;
import com.tmeras.resellmart.address.AddressResponse;
import com.tmeras.resellmart.cart.CartItem;
import com.tmeras.resellmart.cart.CartItemRepository;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.email.EmailService;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.product.ProductResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import com.tmeras.resellmart.user.UserResponse;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderService orderService;

    private CartItem cartItemA;
    private Order orderA;
    private Order orderB;
    private OrderRequest orderRequestA;
    private OrderResponse orderResponseA;
    private OrderResponse orderResponseB;
    private User userA;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        // Initialise test objects
        Role adminRole = new Role(1, "ADMIN");
        Role userRole = new Role(2, "USER");
        userA = TestDataUtils.createUserA(Set.of(adminRole));
        User userB = TestDataUtils.createUserB(Set.of(userRole));
        UserResponse userResponseA = TestDataUtils.createUserResponseA(Set.of(adminRole));
        UserResponse userResponseB = TestDataUtils.createUserResponseB(Set.of(userRole));

        Category category = TestDataUtils.createCategoryA();
        Product productA = TestDataUtils.createProductA(category, userA);
        Product productB = TestDataUtils.createProductB(category, userB);
        CategoryResponse categoryResponse = TestDataUtils.createCategoryResponseA();
        ProductResponse productResponseA = TestDataUtils.createProductResponseA(categoryResponse, userResponseA);
        ProductResponse productResponseB = TestDataUtils.createProductResponseB(categoryResponse, userResponseB);
        cartItemA = new CartItem(1, productA, 1, userA, LocalDateTime.now());

        Address addressA = TestDataUtils.createAddressA(userA);
        Address addressB = TestDataUtils.createAddressB(userB);
        orderA = TestDataUtils.createOrderA(userA, addressA, productB);
        orderB = TestDataUtils.createOrderB(userB, addressB, productA);
        orderRequestA = TestDataUtils.createOrderRequestA(addressA.getId());
        AddressResponse addressResponseA = TestDataUtils.createAddressResponseA(userA.getId());
        AddressResponse addressResponseB = TestDataUtils.createAddressResponseB(userB.getId());
        orderResponseA = TestDataUtils.createOrderResponseA(addressResponseA, userResponseA, productResponseB);
        orderResponseB = TestDataUtils.createOrderResponseB(addressResponseB, userResponseB, productResponseA);

        authentication = new UsernamePasswordAuthenticationToken(
                userA, userA.getPassword(), userA.getAuthorities()
        );
    }

    @Test
    public void shouldSaveOrderWhenValidRequest() throws MessagingException {
        Integer originalProductQuantity = cartItemA.getProduct().getAvailableQuantity();

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(orderA.getBillingAddress()));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.of(orderA.getDeliveryAddress()));
        when(cartItemRepository.findAllWithProductDetailsByUserId(userA.getId()))
                .thenReturn(List.of(cartItemA));
        when(orderRepository.save(any(Order.class))).thenReturn(orderA);
        when(orderMapper.toOrderResponse(orderA)).thenReturn(orderResponseA);

        OrderResponse orderResponse = orderService.save(orderRequestA, authentication);

        assertThat(cartItemA.getProduct().getAvailableQuantity()).isEqualTo(originalProductQuantity - 1);
        verify(cartItemRepository, times(1)).deleteAll(List.of(cartItemA));
        verify(productRepository, times(1)).saveAll(List.of(cartItemA.getProduct()));
        verify(emailService, times(1)).sendOrderConfirmationEmail(userA.getEmail(), orderA);
        assertThat(orderResponse).isEqualTo(orderResponseA);
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidBillingAddressId() {
        orderRequestA.setBillingAddressId(99);

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No billing address found with ID: 99");
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidDeliveryAddressId() {
        orderRequestA.setDeliveryAddressId(99);

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(orderA.getBillingAddress()));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No delivery address found with ID: 99");
    }

    @Test
    public void shouldNotSaveOrderWhenAddressBelongsToDifferentUser() {
        orderRequestA.setBillingAddressId(orderB.getBillingAddress().getId());

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.of(orderA.getDeliveryAddress()));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(orderB.getBillingAddress()));

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("One or both of the specified addresses are related to another user");
    }

    @Test
    public void shouldNotSaveOrderWhenUserCartIsEmpty() {
        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(orderA.getBillingAddress()));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.of(orderA.getDeliveryAddress()));
        when(cartItemRepository.findAllWithProductDetailsByUserId(userA.getId()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("You do not have any items in your cart");
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidCartItemQuantity() {
        cartItemA.setQuantity(99);

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(addressRepository.findWithAssociationsById(orderRequestA.getBillingAddressId()))
                .thenReturn(Optional.of(orderA.getBillingAddress()));
        when(addressRepository.findWithAssociationsById(orderRequestA.getDeliveryAddressId()))
                .thenReturn(Optional.of(orderA.getDeliveryAddress()));
        when(cartItemRepository.findAllWithProductDetailsByUserId(userA.getId()))
                .thenReturn(List.of(cartItemA));

        assertThatThrownBy(() -> orderService.save(orderRequestA, authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("Requested quantity of product with ID '" + cartItemA.getProduct().getId() +
                        "' cannot be larger than available quantity");
    }


}
