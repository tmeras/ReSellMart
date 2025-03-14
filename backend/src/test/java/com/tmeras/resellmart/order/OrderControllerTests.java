package com.tmeras.resellmart.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.address.AddressResponse;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.configuration.TestConfig;
import com.tmeras.resellmart.product.ProductResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.token.JwtFilter;
import com.tmeras.resellmart.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
)
@Import(TestConfig.class)
@WithMockUser(roles = "ADMIN")
public class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderRequest orderRequestA;
    private OrderResponse orderResponseA;
    private OrderResponse orderResponseB;

    @BeforeEach
    public void setup() {
        // Initialise test objects
        UserResponse adminUserResponse = TestDataUtils.createUserResponseA(
                Set.of(new Role(1, "ADMIN"))
        );
        UserResponse userResponse = TestDataUtils.createUserResponseB(
                Set.of(new Role(2, "USER"))
        );

        AddressResponse addressResponseA = TestDataUtils.createAddressResponseA(adminUserResponse.getId());
        AddressResponse addressResponseB = TestDataUtils.createAddressResponseB(userResponse.getId());

        CategoryResponse categoryResponse = TestDataUtils.createCategoryResponseA();
        ProductResponse productResponseA = TestDataUtils.createProductResponseA(categoryResponse, adminUserResponse);
        ProductResponse productResponseB = TestDataUtils.createProductResponseB(categoryResponse, userResponse);

        orderRequestA = TestDataUtils.createOrderRequestA(1);
        orderResponseA = TestDataUtils.createOrderResponseA(
                addressResponseA, adminUserResponse, productResponseB
        );
        orderResponseB = TestDataUtils.createOrderResponseB(
                addressResponseB, userResponse, productResponseA
        );
    }

    @Test
    public void shouldSaveOrderWhenValidRequest() throws Exception {
        when(orderService.save(any(OrderRequest.class), any(Authentication.class))).thenReturn(orderResponseA);

        MvcResult mvcResult = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequestA))
        ).andExpect(status().isCreated()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(orderResponseA));
    }

    @Test
    public void shouldNotSaveOrderWhenInvalidRequest() throws Exception {
        orderRequestA.setPaymentMethod(null);
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("paymentMethod", "Payment method must not be empty");

        MvcResult mvcResult = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequestA))
        ).andExpect(status().isBadRequest()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(expectedErrors));
    }

    @Test
    public void shouldFindAllOrders() throws Exception {
        PageResponse<OrderResponse> pageResponse = new PageResponse<>(
                List.of(orderResponseA, orderResponseB),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                2, 1,
                true, true
        );

        when(orderService.findAll(
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                AppConstants.SORT_ORDERS_BY, AppConstants.SORT_DIR
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldFindAllOrdersByBuyerId() throws Exception {
        PageResponse<OrderResponse> pageResponse = new PageResponse<>(
                List.of(orderResponseA),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                1, 1,
                true, true
        );

        when(orderService.findAllByBuyerId(
                eq(AppConstants.PAGE_NUMBER_INT), eq(AppConstants.PAGE_SIZE_INT),
                eq(AppConstants.SORT_ORDERS_BY), eq(AppConstants.SORT_DIR),
                eq(orderResponseA.getBuyer().getId()), any(Authentication.class)
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/users/" + orderResponseA.getBuyer().getId() + "/orders"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldDeleteOrder() throws Exception {
        mockMvc.perform(delete("/api/orders/" + orderResponseA.getId()))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).delete(orderResponseA.getId());
    }
}
