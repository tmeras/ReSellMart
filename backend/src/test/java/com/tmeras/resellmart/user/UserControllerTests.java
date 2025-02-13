package com.tmeras.resellmart.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.configuration.TestConfig;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.token.JwtFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class)
)
@Import(TestConfig.class)
@WithMockUser(roles = "ADMIN")
public class UserControllerTests {

    public static final Path TEST_PICTURE_PATH = Paths.get("src/test/resources/test_picture.png");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private User userA;
    private UserRequest userRequestA;
    private UserResponse userResponseA;
    private UserResponse userResponseB;

    @BeforeEach
    public void setUp() {
        // Initialise test objects
        Role adminRole = new Role(1, "ADMIN");
        Role userRole = new Role(2, "USER");

        userA = TestDataUtils.createUserA(Set.of(adminRole));
        userRequestA = TestDataUtils.createUserRequestA();
        userResponseA = TestDataUtils.createUserResponseA(Set.of(adminRole));
        userResponseB = TestDataUtils.createUserResponseB(Set.of(userRole));
    }

    @Test
    public void shouldFindUserById() throws Exception {
        when(userService.findById(userA.getId())).thenReturn(userResponseA);

        MvcResult mvcResult = mockMvc.perform(get("/api/users/" + userA.getId()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(userResponseA));
    }

    @Test
    public void shouldFindAllUsers() throws Exception {
        PageResponse<UserResponse> pageResponse = new PageResponse<>(
                List.of(userResponseA, userResponseB),
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                2, 1,
                true, true
        );

        when(userService.findAll(
                AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                AppConstants.SORT_USERS_BY, AppConstants.SORT_DIR
        )).thenReturn(pageResponse);

        MvcResult mvcResult = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(pageResponse));
    }

    @Test
    public void shouldUpdateUserWhenValidRequest() throws Exception {
        userRequestA.setName("Updated user name");
        userResponseA.setName("Updated user name");

        when(userService.update(any(UserRequest.class), eq(userA.getId()), any(Authentication.class)))
                .thenReturn(userResponseA);

        MvcResult mocMvcResult = mockMvc.perform(put("/api/users/" + userA.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(userRequestA))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String jsonResponse = mocMvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(userResponseA));
    }

    @Test
    public void shouldNotUpdateUserWhenInvalidRequest() throws Exception {
        userRequestA.setName(null);
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("name", "Name must not be empty");

        MvcResult mvcResult = mockMvc.perform(put("/api/users/" + userA.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(userRequestA))
        ).andExpect(status().isBadRequest()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(expectedErrors));
    }

    @Test
    public void shouldUploadUserImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test_picture.png",
                "image/png", Files.readAllBytes(TEST_PICTURE_PATH)
        );
        userResponseA.setProfileImage(Files.readAllBytes(TEST_PICTURE_PATH));

        when(userService.uploadUserImage(eq(image), eq(userA.getId()), any(Authentication.class)))
                .thenReturn(userResponseA);

        MvcResult mvcResult = mockMvc.perform(multipart("/api/users/" + userA.getId() + "/image")
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(request -> {request.setMethod("PUT"); return request;})
        ).andExpect(status().isOk()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        assertThat(jsonResponse).isEqualTo(objectMapper.writeValueAsString(userResponseA));
    }



}
