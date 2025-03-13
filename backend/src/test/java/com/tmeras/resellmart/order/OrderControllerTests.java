package com.tmeras.resellmart.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.address.AddressResponse;
import com.tmeras.resellmart.category.CategoryResponse;
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
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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


}
