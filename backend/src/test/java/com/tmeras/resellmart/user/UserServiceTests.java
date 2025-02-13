package com.tmeras.resellmart.user;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.cart.CartItemMapper;
import com.tmeras.resellmart.cart.CartItemRepository;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.mfa.MfaService;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.product.ProductResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.token.TokenRepository;
import com.tmeras.resellmart.wishlist.WishListItemMapper;
import com.tmeras.resellmart.wishlist.WishListItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    public static final Path TEST_PICTURE_PATH = Paths.get("src/test/resources/test_picture.png");

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private WishListItemRepository wishListItemRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private WishListItemMapper wishListItemMapper;

    @Mock
    private FileService fileService;

    @Mock
    private MfaService mfaService;

    @InjectMocks
    private UserService userService;

    private User userA;
    private User userB;
    private UserRequest userRequestA;
    private UserResponse userResponseA;
    private UserResponse userResponseB;
    private Product productA;
    private Product productB;
    private ProductResponse productResponseA;
    private ProductResponse productResponseB;
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        // Initialise test objects
        Role adminRole = new Role(1, "ADMIN");
        Role userRole = new Role(2, "USER");
        userA = TestDataUtils.createUserA(Set.of(adminRole));
        userB = TestDataUtils.createUserB(Set.of(userRole));
        userRequestA = TestDataUtils.createUserRequestA();
        userResponseA = TestDataUtils.createUserResponseA(Set.of(adminRole));
        userResponseB = TestDataUtils.createUserResponseB(Set.of(userRole));

        Category category = TestDataUtils.createCategoryA();
        CategoryResponse categoryResponse = TestDataUtils.createCategoryResponseA();
        productA = TestDataUtils.createProductA(category, userA);
        productB = TestDataUtils.createProductB(category, userB);
        productResponseA = TestDataUtils.createProductResponseA(categoryResponse, userResponseA);
        productResponseB = TestDataUtils.createProductResponseB(categoryResponse, userResponseB);

        authentication = new UsernamePasswordAuthenticationToken(
                userA, userA.getPassword(), userA.getAuthorities()
        );
    }

    @Test
    public void shouldFindUserByIdWhenValidUserId() {
        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(userMapper.toUserResponse(userA)).thenReturn(userResponseA);

        UserResponse userResponse = userService.findById(userA.getId());

        assertThat(userResponse).isEqualTo(userResponseA);
    }

    @Test
    public void shouldNotFindUserByIdWhenInvalidUserId() {
        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(userA.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No user found with ID: " + userA.getId());
    }

    @Test
    public void shouldFindAllUsers() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_USERS_BY).ascending() : Sort.by(AppConstants.SORT_USERS_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<User> page = new PageImpl<>(List.of(userA, userB));

        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toUserResponse(userA)).thenReturn(userResponseA);
        when(userMapper.toUserResponse(userB)).thenReturn(userResponseB);

        PageResponse<UserResponse> pageResponse =
                userService.findAll(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_USERS_BY, AppConstants.SORT_DIR);

        assertThat(pageResponse.getContent().size()).isEqualTo(2);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(userResponseA);
        assertThat(pageResponse.getContent().get(1)).isEqualTo(userResponseB);
    }

    @Test
    public void shouldUpdateUserWhenValidRequest() {
        userRequestA.setName("Updated user name");
        userRequestA.setHomeCountry("Updated home country");
        userRequestA.setMfaEnabled(true);
        userResponseA.setName("Updated user name");
        userResponseA.setHomeCountry("Updated home country");
        userResponseA.setMfaEnabled(true);
        userResponseA.setQrImageUri("uri");

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(mfaService.generateSecret()).thenReturn("secret");
        when(mfaService.generateQrCodeImageUri("secret", userA.getEmail())).thenReturn("uri");
        when(userRepository.save(userA)).thenReturn(userA);
        when(userMapper.toUserResponse(userA)).thenReturn(userResponseA);

        UserResponse userResponse = userService.update(userRequestA, userA.getId(), authentication);

        assertThat(userResponse).isEqualTo(userResponseA);
        assertThat(userA.getRealName()).isEqualTo(userRequestA.getName());
        assertThat(userA.getHomeCountry()).isEqualTo(userRequestA.getHomeCountry());
        assertThat(userA.isMfaEnabled()).isEqualTo(userRequestA.isMfaEnabled());
        assertThat(userA.getSecret()).isEqualTo("secret");
    }

    @Test
    public void shouldNotUpdateUserWhenUserIsNotLoggedIn() {
        assertThatThrownBy(() -> userService.update(userRequestA, userB.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to update the details of this user");
    }

    @Test
    public void shouldUploadUserImageWhenValidRequest() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test_picture.png",
                "image/png", Files.readAllBytes(TEST_PICTURE_PATH)
        );
        userResponseA.setProfileImage(Files.readAllBytes(TEST_PICTURE_PATH));

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(fileService.getFileExtension(image.getOriginalFilename())).thenReturn("png");
        when(fileService.saveUserImage(image, userA.getId())).thenReturn("/uploads/test_picture.png");
        when(userRepository.save(userA)).thenReturn(userA);
        when(userMapper.toUserResponse(userA)).thenReturn(userResponseA);

        UserResponse userResponse = userService.uploadUserImage(image, userA.getId(), authentication);

        assertThat(userResponse).isEqualTo(userResponseA);
        assertThat(userA.getImagePath()).isEqualTo("/uploads/test_picture.png");
    }

    @Test
    public void shouldNotUploadUserImageWhenUserIsNotLoggedIn() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test_picture.png",
                "image/png", Files.readAllBytes(TEST_PICTURE_PATH)
        );

        assertThatThrownBy(() -> userService.uploadUserImage(image, userB.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to update this user's profile image");
    }



}
