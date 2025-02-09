package com.tmeras.resellmart.user;

import com.tmeras.resellmart.cart.*;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.APIException;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.file.FileService;
import com.tmeras.resellmart.mfa.MfaService;
import com.tmeras.resellmart.product.Product;
import com.tmeras.resellmart.product.ProductRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final WishListItemRepository wishListItemRepository;
    private final UserMapper userMapper;
    private final CartItemMapper cartItemMapper;
    private final WishListItemMapper wishListItemMapper;
    private final FileService fileService;
    private final MfaService mfaService;

    public UserResponse findById(Integer userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with ID " + userId));
    }

    @PreAuthorize("hasRole('ADMIN')") // Only admins should be able to view all users
    public PageResponse<UserResponse> findAll(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<User> users = userRepository.findAll(pageable);
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

    public UserResponse update(UserRequest userRequest, Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to update the details of this user");

        // If enabling MFA, generate QR image
        String qrImageUri = null;
        if (!currentUser.isMfaEnabled() && userRequest.isMfaEnabled()) {
            currentUser.setSecret(mfaService.generateSecret());
            qrImageUri = mfaService.generateQrCodeImageUri(currentUser.getSecret(), currentUser.getEmail());
        }

        currentUser.setName(userRequest.getName());
        currentUser.setHomeCountry(userRequest.getHomeCountry());
        currentUser.setMfaEnabled(userRequest.isMfaEnabled());

        User updatedUser = userRepository.save(currentUser);
        UserResponse userResponse = userMapper.toUserResponse(updatedUser);
        userResponse.setQrImageUri(qrImageUri);
        return userResponse;
    }


    public void uploadUserImage(MultipartFile image, Integer userId, Authentication authentication) throws IOException {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to update this user's profile image");

        // Delete previous user image, if it exists
        if (currentUser.getImagePath() != null)
            fileService.deleteFile(currentUser.getImagePath());

        String fileName = image.getOriginalFilename();
        String fileExtension = fileService.getFileExtension(fileName);
        Set<String> validImageExtensions = Set.of("jpg", "jpeg", "png", "gif", "bmp", "tiff");
        if (!validImageExtensions.contains(fileExtension))
            throw new APIException("Only images can be uploaded");

        String filePath = fileService.saveUserImage(image, userId);
        currentUser.setImagePath(filePath);
        userRepository.save(currentUser);
    }

    public CartItemResponse saveCartItem(CartItemRequest cartItemRequest, Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to add items to this user's cart");

        if (cartItemRepository.existsByUserIdAndProductId(userId, cartItemRequest.getProductId()))
            throw new APIException("This product is already in your cart");

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findById(userId).get();
        Product existingProduct = productRepository.findWithAssociationsById(cartItemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + cartItemRequest.getProductId()));

        if(Objects.equals(existingProduct.getSeller().getId(), currentUser.getId()))
            throw new APIException("You cannot add your own items to your cart");

        if(!existingProduct.isAvailable())
            throw new APIException("Unavailable products cannot be added to the cart");

        if (existingProduct.getAvailableQuantity() < cartItemRequest.getQuantity())
            throw new APIException("Requested product quantity cannot be higher than available quantity");

        CartItem cartItem = cartItemMapper.toCartItem(cartItemRequest);
        cartItem.setProduct(existingProduct);
        cartItem.setUser(currentUser);
        cartItem.setAddedAt(LocalDateTime.now());

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return cartItemMapper.toCartItemResponse(savedCartItem);
    }

    public List<CartItemResponse> findAllCartItemsByUserId(Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to view this user's cart");

        List<CartItem> cartItems = cartItemRepository.findAllWithAssociationsByUserId(userId);

        return cartItems.stream()
                .map(cartItemMapper::toCartItemResponse)
                .toList();
    }

    public CartItemResponse updateCartItemQuantity(
            CartItemRequest cartItemRequest, Integer userId, Integer productId, Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(currentUser.getId(), userId))
            throw new OperationNotPermittedException("You do not have permission to modify this user's cart");

        CartItem existingCartItem = cartItemRepository.findWithAssociationsByUserIdAndProductId(userId, productId)
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
            throw new APIException("This product is already in your wishlist");

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findById(userId).get();
        Product existingProduct = productRepository.findWithAssociationsById(wishListItemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("No product found with ID: " + wishListItemRequest.getProductId()));

        if(Objects.equals(existingProduct.getSeller().getId(), currentUser.getId()))
            throw new APIException("You cannot add your own items to your wishlist");

        if(!existingProduct.isAvailable())
            throw new APIException("Unavailable products cannot be added to the wishlist");

        WishListItem wishListItem = new WishListItem();
        wishListItem.setProduct(existingProduct);
        wishListItem.setUser(currentUser);
        wishListItem.setAddedAt(LocalDateTime.now());

        WishListItem savedWishListItem = wishListItemRepository.save(wishListItem);
        return wishListItemMapper.toWishListItemResponse(savedWishListItem);

    }
}
