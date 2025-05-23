package com.tmeras.resellmart.user;

import com.tmeras.resellmart.cart.*;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceAlreadyExistsException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
import com.tmeras.resellmart.security.MfaService;
import com.tmeras.resellmart.token.Token;
import com.tmeras.resellmart.token.TokenRepository;
import com.tmeras.resellmart.wishlist.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static com.tmeras.resellmart.common.AppConstants.ACCEPTED_IMAGE_EXTENSIONS;
import static com.tmeras.resellmart.common.AppConstants.FLYWAY_USERS_NUMBER;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final WishListItemRepository wishListItemRepository;
    private final UserMapper userMapper;
    private final CartItemMapper cartItemMapper;
    private final WishListItemMapper wishListItemMapper;
    private final FileService fileService;
    private final MfaService mfaService;

    public UserResponse findById(Integer userId) {
        return userRepository.findWithAssociationsById(userId)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with ID: " + userId));
    }

    @PreAuthorize("hasRole('ADMIN')") // Only admins should be able to view all users
    public PageResponse<UserResponse> findAll(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<User> users = userRepository.findAll(pageable);
        // Initialize lazy associations
        for (User user : users)
            user.getRoles().size();
        List<UserResponse> userResponses = users.stream()
                .map(userMapper::toUserResponse)
                .toList();

        return new PageResponse<>(
                userResponses,
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isFirst(),
                users.isLast()
        );
    }

    public UserResponse findMe(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        return userMapper.toUserResponse(currentUser);
    }

    public UserResponse update(UserRequest userRequest, Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to update the details of this user");

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findWithAssociationsById(userId).get();

        // If enabling MFA, generate QR image
        String qrImageUri = null;
        if (!currentUser.getIsMfaEnabled() && userRequest.getIsMfaEnabled()) {
            currentUser.setSecret(mfaService.generateSecret());
            qrImageUri = mfaService.generateQrCodeImageUri(currentUser.getSecret(), currentUser.getEmail());
        }

        currentUser.setName(userRequest.getName());
        currentUser.setHomeCountry(userRequest.getHomeCountry());
        currentUser.setIsMfaEnabled(userRequest.getIsMfaEnabled());

        User updatedUser = userRepository.save(currentUser);
        UserResponse userResponse = userMapper.toUserResponse(updatedUser);
        userResponse.setQrImageUri(qrImageUri);
        return userResponse;
    }

    public UserResponse uploadUserImage(MultipartFile image, Integer userId, Authentication authentication) throws IOException {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to update this user's profile image");

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findWithAssociationsById(userId).get();

        // Delete previous user image, if it exists and if user wasn't created using flyway script
        if (userId > FLYWAY_USERS_NUMBER && currentUser.getImagePath() != null)
            fileService.deleteFile(currentUser.getImagePath());

        if (image == null) {
            currentUser.setImagePath(null);
        } else {
            String fileName = image.getOriginalFilename();
            String fileExtension = fileService.getFileExtension(fileName);
            if (!ACCEPTED_IMAGE_EXTENSIONS.contains(fileExtension))
                throw new APIException("Only images can be uploaded");

            String filePath = fileService.saveUserImage(image, userId);
            currentUser.setImagePath(filePath);
        }

        currentUser = userRepository.save(currentUser);
        return userMapper.toUserResponse(currentUser);
    }

    public CartItemResponse saveCartItem(CartItemRequest cartItemRequest, Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to add items to this user's cart");

        if (cartItemRepository.existsByUserIdAndProductId(userId, cartItemRequest.getProductId()))
            throw new ResourceAlreadyExistsException("This product is already in your cart");

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findById(userId).get();
        Product existingProduct = productRepository.findWithAssociationsById(cartItemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + cartItemRequest.getProductId()));

        if (Objects.equals(existingProduct.getSeller().getId(), currentUser.getId()))
            throw new APIException("You cannot add your own items to your cart");

        if (Boolean.TRUE.equals(existingProduct.getIsDeleted()))
            throw new APIException("Deleted products cannot be added to the cart");

        if (existingProduct.getAvailableQuantity() < cartItemRequest.getQuantity())
            throw new APIException("Requested product quantity cannot be higher than available quantity");

        CartItem cartItem = cartItemMapper.toCartItem(cartItemRequest);
        cartItem.setProduct(existingProduct);
        cartItem.setUser(currentUser);
        cartItem.setAddedAt(ZonedDateTime.now());

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return cartItemMapper.toCartItemResponse(savedCartItem);
    }

    public List<CartItemResponse> findAllCartItemsByUserId(Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to view this user's cart");

        List<CartItem> cartItems = cartItemRepository.findAllWithProductDetailsByUserId(userId);

        return cartItems.stream()
                .map(cartItemMapper::toCartItemResponse)
                .toList();
    }

    public BigDecimal calculateCartTotal(Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to view this user's cart total");

        List<CartItem> cartItems = cartItemRepository.findAllWithProductDetailsByUserId(userId);

        return cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public CartItemResponse updateCartItemQuantity(
            CartItemRequest cartItemRequest, Integer userId, Integer productId, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to modify this user's cart");

        CartItem existingCartItem = cartItemRepository.findWithProductDetailsByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("The specified product does not exist in your cart"));

        if (existingCartItem.getProduct().getAvailableQuantity() < cartItemRequest.getQuantity())
            throw new APIException("Requested product quantity cannot be higher than available quantity");

        existingCartItem.setQuantity(cartItemRequest.getQuantity());

        existingCartItem = cartItemRepository.save(existingCartItem);
        return cartItemMapper.toCartItemResponse(existingCartItem);
    }

    public void deleteCartItem(Integer userId, Integer productId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to modify this user's cart");

        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public WishListItemResponse saveWishListItem(
            WishListItemRequest wishListItemRequest, Integer userId, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to add items to this user's wishlist");

        if (wishListItemRepository.existsByUserIdAndProductId(userId, wishListItemRequest.getProductId()))
            throw new ResourceAlreadyExistsException("This product is already in your wishlist");

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findById(userId).get();
        Product existingProduct = productRepository.findWithAssociationsById(wishListItemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + wishListItemRequest.getProductId()));

        if (Objects.equals(existingProduct.getSeller().getId(), currentUser.getId()))
            throw new APIException("You cannot add your own items to your wishlist");

        WishListItem wishListItem = new WishListItem();
        wishListItem.setProduct(existingProduct);
        wishListItem.setUser(currentUser);
        wishListItem.setAddedAt(ZonedDateTime.now());

        WishListItem savedWishListItem = wishListItemRepository.save(wishListItem);
        return wishListItemMapper.toWishListItemResponse(savedWishListItem);
    }

    public List<WishListItemResponse> findAllWishListItemsByUserId(Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to view this user's wishlist");

        List<WishListItem> wishListItems = wishListItemRepository.findAllWithProductDetailsByUserId(userId);

        return wishListItems.stream()
                .map(wishListItemMapper::toWishListItemResponse)
                .toList();
    }

    public void deleteWishListItem(Integer userId, Integer productId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to modify this user's wishlist");

        wishListItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public void disable(Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        boolean isCurrentUserAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        // Users can disable (soft-delete) their own accounts and admins can disable any non-admin user
        if (!Objects.equals(currentUser.getId(), userId) && !isCurrentUserAdmin)
            throw new OperationNotPermittedException("You do not have permission to disable this user");

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with ID: " + userId));
        boolean isExistingUserAdmin = existingUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        if (isExistingUserAdmin)
            throw new APIException("You cannot disable an admin user");

        // Disable user
        existingUser.setIsEnabled(false);
        userRepository.save(existingUser);

        // Revoke all refresh tokens belonging to the user
        List<Token> refreshToken = tokenRepository.findAllValidRefreshTokensByUserEmail(existingUser.getEmail());
        refreshToken.forEach(token -> token.setIsRevoked(true));
        tokenRepository.saveAll(refreshToken);

        // Mark all user products as unavailable
        List<Product> userProducts = productRepository.findAllBySellerId(userId);
        userProducts.forEach(product -> product.setIsDeleted(false));

        productRepository.saveAll(userProducts);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void enable(Integer userId) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with ID: " + userId));

        existingUser.setIsEnabled(true);
        userRepository.save(existingUser);
    }
}
