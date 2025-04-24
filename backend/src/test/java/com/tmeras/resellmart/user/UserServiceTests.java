package com.tmeras.resellmart.user;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.cart.*;
import com.tmeras.resellmart.category.Category;
import com.tmeras.resellmart.category.CategoryResponse;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceAlreadyExistsException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.product.ProductResponse;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.security.MfaService;
import com.tmeras.resellmart.token.Token;
import com.tmeras.resellmart.token.TokenRepository;
import com.tmeras.resellmart.token.TokenType;
import com.tmeras.resellmart.wishlist.*;
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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    public void shouldFindLoggedInUser() {
        when(userMapper.toUserResponse(userA)).thenReturn(userResponseA);

        UserResponse userResponse = userService.findMe(authentication);

        assertThat(userResponse).isEqualTo(userResponseA);
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

    @Test
    public void shouldNotUploadUserImageWhenInvalidFileExtension() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test_file.txt",
                "plain/text", Files.readAllBytes(Path.of("src/test/resources/test_file.txt"))
        );

        when(userRepository.findWithAssociationsById(userA.getId())).thenReturn(Optional.of(userA));
        when(fileService.getFileExtension(image.getOriginalFilename())).thenReturn("txt");

        assertThatThrownBy(() -> userService.uploadUserImage(image, userA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("Only images can be uploaded");
    }

    @Test
    public void shouldSaveCartItemWhenValidRequest() {
        CartItem cartItem = new CartItem(1, productB, 1, userA, ZonedDateTime.now());
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 1, userA.getId());
        CartItemResponse cartItemResponse = new CartItemResponse(1, productResponseB, 1, ZonedDateTime.now());

        when(cartItemRepository.existsByUserIdAndProductId(userA.getId(), productB.getId())).thenReturn(false);
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(productRepository.findWithAssociationsById(productB.getId())).thenReturn(Optional.of(productB));
        when(cartItemMapper.toCartItem(cartItemRequest)).thenReturn(cartItem);
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartItemMapper.toCartItemResponse(cartItem)).thenReturn(cartItemResponse);

        CartItemResponse response = userService.saveCartItem(cartItemRequest, userA.getId(), authentication);

        assertThat(response).isEqualTo(cartItemResponse);
        assertThat(cartItem.getProduct()).isEqualTo(productB);
        assertThat(cartItem.getUser()).isEqualTo(userA);
    }

    @Test
    public void shouldNotSaveCartItemWhenCartOwnerIsNotLoggedIn() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 1, userB.getId());

        assertThatThrownBy(() -> userService.saveCartItem(cartItemRequest, userB.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to add items to this user's cart");
    }

    @Test
    public void shouldNotSaveCartItemWhenDuplicateCartItem() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 1, userA.getId());

        when(cartItemRepository.existsByUserIdAndProductId(userA.getId(), productB.getId())).thenReturn(true);

        assertThatThrownBy(() -> userService.saveCartItem(cartItemRequest, userA.getId(), authentication))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("This product is already in your cart");
    }

    @Test
    public void shouldNotSaveCartItemWhenInvalidProductId() {
        CartItemRequest cartItemRequest = new CartItemRequest(1000, 1, userA.getId());

        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(productRepository.findWithAssociationsById(1000)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.saveCartItem(cartItemRequest, userA.getId(), authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product found with ID: 1000");
    }

    @Test
    public void shouldNotSaveCartItemWhenSellerIsLoggedIn() {
        CartItemRequest cartItemRequest = new CartItemRequest(productA.getId(), 1, userA.getId());

        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));

        assertThatThrownBy(() -> userService.saveCartItem(cartItemRequest, userA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("You cannot add your own items to your cart");
    }

    @Test
    public void shouldNotSaveCartItemWhenProductIsDeleted() {
        productB.setIsDeleted(true);
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 5, userA.getId());

        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(productRepository.findWithAssociationsById(productB.getId())).thenReturn(Optional.of(productB));

        assertThatThrownBy(() -> userService.saveCartItem(cartItemRequest, userA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("Deleted products cannot be added to the cart");
    }

    @Test
    public void shouldNotSaveCartItemWhenInvalidQuantity() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 99, userA.getId());

        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(productRepository.findWithAssociationsById(productB.getId())).thenReturn(Optional.of(productB));

        assertThatThrownBy(() -> userService.saveCartItem(cartItemRequest, userA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("Requested product quantity cannot be higher than available quantity");
    }

    @Test
    public void shouldFindAllCartItemsByUserIdWhenValidRequest() {
        CartItem cartItem = new CartItem(1, productB, 1, userA, ZonedDateTime.now());
        CartItemResponse cartItemResponse = new CartItemResponse(1, productResponseB, 1, ZonedDateTime.now());

        when(cartItemRepository.findAllWithProductDetailsByUserId(userA.getId()))
                .thenReturn(List.of(cartItem));
        when(cartItemMapper.toCartItemResponse(cartItem)).thenReturn(cartItemResponse);

        List<CartItemResponse> cartItemResponses = userService.findAllCartItemsByUserId(userA.getId(), authentication);

        assertThat(cartItemResponses).hasSize(1);
        assertThat(cartItemResponses.get(0)).isEqualTo(cartItemResponse);
    }

    @Test
    public void shouldNotFindAllCartItemsByUserIdWhenCartOwnerIsNotLoggedIn() {
        assertThatThrownBy(() -> userService.findAllCartItemsByUserId(userB.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to view this user's cart");
    }

    @Test
    public void shouldUpdateCartItemQuantityWhenValidRequest() {
        CartItem cartItem = new CartItem(1, productB, 1, userA, ZonedDateTime.now());
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 2, userA.getId());
        CartItemResponse cartItemResponse = new CartItemResponse(1, productResponseB, 2, ZonedDateTime.now());

        when(cartItemRepository.findWithProductDetailsByUserIdAndProductId(userA.getId(), productB.getId()))
                .thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartItemMapper.toCartItemResponse(cartItem)).thenReturn(cartItemResponse);

        CartItemResponse response =
                userService.updateCartItemQuantity(cartItemRequest, userA.getId(), productB.getId(), authentication);

        assertThat(response).isEqualTo(cartItemResponse);
        assertThat(cartItem.getQuantity()).isEqualTo(cartItemRequest.getQuantity());
    }

    @Test
    public void shouldNotUpdateCartItemQuantityWhenUserIsNotLoggedIn() {
        CartItemRequest cartItemRequest = new CartItemRequest(productA.getId(), 2, userB.getId());

        assertThatThrownBy(() ->
                userService.updateCartItemQuantity(cartItemRequest, userB.getId(), productA.getId(), authentication)
        ).isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to modify this user's cart");
    }

    @Test
    public void shouldNotUpdateCartItemQuantityWhenCartItemDoesntExist() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 2, userA.getId());

        when(cartItemRepository.findWithProductDetailsByUserIdAndProductId(userA.getId(), productB.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.updateCartItemQuantity(cartItemRequest, userA.getId(), productB.getId(), authentication)
        ).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("The specified product does not exist in your cart");
    }

    @Test
    public void shouldNotUpdateCartItemQuantityWhenInvalidQuantity() {
        CartItemRequest cartItemRequest = new CartItemRequest(productB.getId(), 99, userA.getId());
        CartItem cartItem = new CartItem(1, productB, 5, userA, ZonedDateTime.now());

        when(cartItemRepository.findWithProductDetailsByUserIdAndProductId(userA.getId(), productB.getId()))
                .thenReturn(Optional.of(cartItem));

        assertThatThrownBy(() ->
                userService.updateCartItemQuantity(cartItemRequest, userA.getId(), productB.getId(), authentication)
        ).isInstanceOf(APIException.class)
                .hasMessage("Requested product quantity cannot be higher than available quantity");
    }

    @Test
    public void shouldDeleteCartItemWhenValidRequest() {
        userService.deleteCartItem(userA.getId(), productB.getId(), authentication);

        verify(cartItemRepository, times(1))
                .deleteByUserIdAndProductId(userA.getId(), productB.getId());
    }

    @Test
    public void shouldNotDeleteCartItemWhenCartOwnerIsNotLoggedIn() {
        assertThatThrownBy(() -> userService.deleteCartItem(userB.getId(), productA.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to modify this user's cart");
    }

    @Test
    public void shouldSaveWishListItemWhenValidRequest() {
        WishListItem wishListItem = new WishListItem(1, ZonedDateTime.now(), productB, userA);
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productB.getId(), userA.getId());
        WishListItemResponse wishListItemResponse = new WishListItemResponse(1, productResponseB, ZonedDateTime.now());

        when(wishListItemRepository.existsByUserIdAndProductId(userA.getId(), productB.getId())).thenReturn(false);
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(productRepository.findWithAssociationsById(productB.getId())).thenReturn(Optional.of(productB));
        when(wishListItemRepository.save(any(WishListItem.class))).thenReturn(wishListItem);
        when(wishListItemMapper.toWishListItemResponse(wishListItem)).thenReturn(wishListItemResponse);

        WishListItemResponse response = userService.saveWishListItem(wishListItemRequest, userA.getId(), authentication);

        assertThat(response).isEqualTo(wishListItemResponse);
    }

    @Test
    public void shouldNotSaveWishListItemWhenListOwnerIsNotLoggedIn() {
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productB.getId(), userA.getId());

        assertThatThrownBy(() -> userService.saveWishListItem(wishListItemRequest, userB.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to add items to this user's wishlist");
    }

    @Test
    public void shouldNotSaveWisListItemWhenDuplicateWishListItem() {
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productB.getId(), userA.getId());

        when(wishListItemRepository.existsByUserIdAndProductId(userA.getId(), productB.getId())).thenReturn(true);

        assertThatThrownBy(() -> userService.saveWishListItem(wishListItemRequest, userA.getId(), authentication))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("This product is already in your wishlist");
    }

    @Test
    public void shouldNotSaveWishListItemWhenInvalidProductId() {
        WishListItemRequest wishListItemRequest = new WishListItemRequest(99, userA.getId());

        when(wishListItemRepository.existsByUserIdAndProductId(userA.getId(), 99)).thenReturn(false);
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(productRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.saveWishListItem(wishListItemRequest, userA.getId(), authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No product found with ID: 99");
    }

    @Test
    public void shouldNotSaveWishListItemWhenSellerIsLoggedIn() {
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productA.getId(), userA.getId());

        when(wishListItemRepository.existsByUserIdAndProductId(userA.getId(), productA.getId())).thenReturn(false);
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(productRepository.findWithAssociationsById(productA.getId())).thenReturn(Optional.of(productA));

        assertThatThrownBy(() -> userService.saveWishListItem(wishListItemRequest, userA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("You cannot add your own items to your wishlist");
    }

    @Test
    public void shouldNotSaveWishListItemWhenProductIsDeleted() {
        productB.setIsDeleted(true);
        WishListItemRequest wishListItemRequest = new WishListItemRequest(productB.getId(), userA.getId());

        when(wishListItemRepository.existsByUserIdAndProductId(userA.getId(), productB.getId())).thenReturn(false);
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(productRepository.findWithAssociationsById(productB.getId())).thenReturn(Optional.of(productB));

        assertThatThrownBy(() -> userService.saveWishListItem(wishListItemRequest, userA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("Deleted products cannot be added to the wishlist");
    }

    @Test
    public void shouldFindAllWishListItemsByUserIdWhenValidRequest() {
        WishListItem wishListItem = new WishListItem(1, ZonedDateTime.now(), productB, userA);
        WishListItemResponse wishListItemResponse = new WishListItemResponse(1, productResponseB, ZonedDateTime.now());

        when(wishListItemRepository.findAllWithProductDetailsByUserId(userA.getId()))
                .thenReturn(List.of(wishListItem));
        when(wishListItemMapper.toWishListItemResponse(wishListItem)).thenReturn(wishListItemResponse);

        List<WishListItemResponse> wishListItemResponses = userService.findAllWishListItemsByUserId(userA.getId(), authentication);

        assertThat(wishListItemResponses).hasSize(1);
        assertThat(wishListItemResponses.get(0)).isEqualTo(wishListItemResponse);
    }

    @Test
    public void shouldNotFindAllWishListItemsByUserIdWhenListOwnerIsNotLoggedIn() {
        assertThatThrownBy(() -> userService.findAllWishListItemsByUserId(userB.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to view this user's wishlist");
    }

    @Test
    public void shouldDeleteWishListItemWhenValidRequest() {
        userService.deleteWishListItem(userA.getId(), productB.getId(), authentication);

        verify(wishListItemRepository, times(1))
                .deleteByUserIdAndProductId(userA.getId(), productB.getId());
    }

    @Test
    public void shouldNotDeleteWishListItemWhenListOwnerIsNotLoggedIn() {
        assertThatThrownBy(() -> userService.deleteWishListItem(userB.getId(), productA.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to modify this user's wishlist");
    }

    @Test
    public void shouldDisableUserWhenValidRequest() {
        Token testToken = new Token(1, "token", TokenType.BEARER, LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(1), null, false, userA);
        // Downgrade admin user
        userA.setRoles(Set.of(new Role(1, "USER")));

        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(tokenRepository.findAllValidRefreshTokensByUserEmail(userA.getEmail())).thenReturn(List.of(testToken));
        when(productRepository.findAllBySellerId(userA.getId())).thenReturn(List.of(productA));

        userService.disable(userA.getId(), authentication);

        assertThat(userA.isEnabled()).isFalse();
        assertThat(testToken.isRevoked()).isTrue();
        assertThat(productA.getIsDeleted()).isFalse();
    }

    @Test
    public void shouldNotDisableUserWhenUserOrAdminIsNotLoggedIn() {
        // Downgrade admin user
        userA.setRoles(Set.of(new Role(1, "USER")));

        assertThatThrownBy(() -> userService.disable(userB.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to disable this user");
    }

    @Test
    public void shouldNotDisableUserWhenInvalidUserId() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.disable(99, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No user found with ID: 99");
    }

    @Test
    public void shouldNotDisableAdminUser() {
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));

        assertThatThrownBy(() -> userService.disable(userA.getId(), authentication))
                .isInstanceOf(APIException.class)
                .hasMessage("You cannot disable an admin user");
    }

    @Test
    public void shouldEnableUserWhenValidRequest() {
        userA.setEnabled(false);

        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));

        userService.enable(userA.getId());

        assertThat(userA.isEnabled()).isTrue();
    }

    @Test
    public void shouldNotEnableUserWhenInvalidUserId() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.enable(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No user found with ID: 99");
    }
}
