package com.tmeras.resellmart.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.configuration.TestConfig;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.token.JwtFilter;
import com.tmeras.resellmart.user.User;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthenticationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
)
@Import(TestConfig.class)
public class AuthenticationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    private User userA;

    @BeforeEach
    public void setUp() {
        // Initialise test objects
        Role adminRole = new Role(1, "ADMIN");
        userA = TestDataUtils.createUserA(Set.of(adminRole));
    }

    @Test
    public void shouldRegisterUserWhenValidRequest() throws Exception {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .name("Test name")
                .email("test@test.com")
                .password("Pass123!")
                .homeCountry("UK")
                .mfaEnabled(true)
                .build();
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .mfaEnabled(true)
                .qrImageUri("uri")
                .build();

        when(authenticationService.register(any(RegistrationRequest.class))).thenReturn(authenticationResponse);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
        ).andExpect(status().isCreated()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(authenticationResponse));
    }

    @Test
    public void shouldNotRegisterUserWhenInvalidRequest() throws Exception {
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .name(null)
                .email("test@test.com")
                .password("Pass1!")
                .homeCountry("UK")
                .mfaEnabled(true)
                .build();
        Map<String, String> expectedErrors = new java.util.HashMap<>();
        expectedErrors.put("name", "Name must not be empty");
        expectedErrors.put("password", "Password must be at least 8 characters long");

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
        ).andExpect(status().isBadRequest()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(expectedErrors));
    }

    @Test
    public void shouldLoginUserWhenValidRequest() throws Exception {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("test@test.com")
                .password("pass")
                .build();
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        when(authenticationService.login(any(AuthenticationRequest.class))).thenReturn(authenticationResponse);

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest))
        ).andExpect(status().isOk()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(authenticationResponse));
    }

    @Test
    public void shouldNotLoginUserWhenInvalidRequest() throws Exception {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("test@test.com")
                .password(null)
                .build();
        Map<String, String> expectedErrors = new java.util.HashMap<>();
        expectedErrors.put("password", "Password must not be empty");

        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest))
        ).andExpect(status().isBadRequest()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(expectedErrors));
    }

}
