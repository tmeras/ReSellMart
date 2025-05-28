package com.tmeras.resellmart.user;

import com.tmeras.resellmart.cart.CartItemRequest;
import com.tmeras.resellmart.cart.CartItemResponse;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.wishlist.WishListItemRequest;
import com.tmeras.resellmart.wishlist.WishListItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{user-id}")
    public ResponseEntity<UserResponse> findById(@PathVariable(name = "user-id") Integer userId) {
        UserResponse foundUser = userService.findById(userId);
        return new ResponseEntity<>(foundUser, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> findAll(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection,
            @RequestParam(name = "search", required = false) String search
    ) {
        PageResponse<UserResponse> foundUsers = (search == null || search.isBlank() ?
                userService.findAll(pageNumber, pageSize, sortBy, sortDirection)
                : userService.findAllByKeyword(pageNumber, pageSize, sortBy, sortDirection, search));
        return new ResponseEntity<>(foundUsers, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> findMe(Authentication authentication) {
        UserResponse userResponse = userService.findMe(authentication);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @PutMapping("/{user-id}")
    public ResponseEntity<UserResponse> update(
            @Valid @RequestBody UserRequest userRequest,
            @PathVariable(name = "user-id") Integer userId,
            Authentication authentication
    ) {
        UserResponse updatedUser = userService.update(userRequest, userId, authentication);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PutMapping("/{user-id}/image")
    public ResponseEntity<UserResponse> uploadUserImage(
            @PathVariable(name = "user-id") Integer userId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication
    ) throws IOException {
        UserResponse updatedUser = userService.uploadUserImage(image, userId, authentication);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PostMapping("/{user-id}/cart/products")
    public ResponseEntity<CartItemResponse> saveCartItem(
            @Valid @RequestBody CartItemRequest cartItemRequest,
            @PathVariable(name = "user-id") Integer userId,
            Authentication authentication
    ) {
        CartItemResponse savedCartItem = userService.saveCartItem(cartItemRequest, userId, authentication);
        return new ResponseEntity<>(savedCartItem, HttpStatus.CREATED);
    }

    @GetMapping("/{user-id}/cart/products")
    public ResponseEntity<List<CartItemResponse>> findAllCartItemsByUserId(
        @PathVariable(name = "user-id") Integer userId,
        Authentication authentication
    ) {
        List<CartItemResponse> foundCartItems = userService.findAllCartItemsByUserId(userId, authentication);
        return new ResponseEntity<>(foundCartItems, HttpStatus.OK);
    }

    @GetMapping("/{user-id}/cart/total")
    public ResponseEntity<BigDecimal> calculateCartTotal(
            @PathVariable(name = "user-id") Integer userId,
            Authentication authentication
    ) {
        BigDecimal cartTotal = userService.calculateCartTotal(userId, authentication);
        return new ResponseEntity<>(cartTotal, HttpStatus.OK);
    }

    @PatchMapping("/{user-id}/cart/products/{product-id}")
    public ResponseEntity<CartItemResponse> updateCartItemQuantity(
        @Valid @RequestBody CartItemRequest cartItemRequest,
        @PathVariable(name = "user-id") Integer userId,
        @PathVariable(name = "product-id") Integer productId,
        Authentication authentication
    ) {
        CartItemResponse updatedCartItem =
                userService.updateCartItemQuantity(cartItemRequest, userId, productId, authentication);
        return  new ResponseEntity<>(updatedCartItem, HttpStatus.OK);
    }

    @DeleteMapping("/{user-id}/cart/products/{product-id}")
    public ResponseEntity<?> deleteCartItem(
            @PathVariable(name = "user-id") Integer userId,
            @PathVariable(name = "product-id") Integer productId,
            Authentication authentication
    ) {
        userService.deleteCartItem(userId, productId, authentication);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{user-id}/wishlist/products")
    public ResponseEntity<WishListItemResponse> saveWishListItem(
        @Valid @RequestBody WishListItemRequest wishListItemRequest,
        @PathVariable(name = "user-id") Integer userId,
        Authentication authentication
    ) {
        WishListItemResponse savedWishListItem =
                userService.saveWishListItem(wishListItemRequest, userId, authentication);
        return new ResponseEntity<>(savedWishListItem, HttpStatus.CREATED);
    }

    @GetMapping("/{user-id}/wishlist/products")
    public ResponseEntity<List<WishListItemResponse>> findAllWishListItemsByUserId(
            @PathVariable(name = "user-id") Integer userId,
            Authentication authentication
    ) {
        List<WishListItemResponse> foundWishListItems = userService.findAllWishListItemsByUserId(userId, authentication);
        return new ResponseEntity<>(foundWishListItems, HttpStatus.OK);
    }

    @DeleteMapping("/{user-id}/wishlist/products/{product-id}")
    public ResponseEntity<?> deleteWishListItem(
            @PathVariable(name = "user-id") Integer userId,
            @PathVariable(name = "product-id") Integer productId,
            Authentication authentication
    ) {
        userService.deleteWishListItem(userId, productId, authentication);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{user-id}/activation")
    public ResponseEntity<?> enableOrDisableUser(
            @Valid @RequestBody UserEnableRequest userEnableRequest,
            @PathVariable(name = "user-id") Integer userId,
            Authentication authentication
    ) {
        if (userEnableRequest.getIsEnabled())
            userService.enable(userId);
        else
            userService.disable(userId, authentication);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{user-id}/promote")
    public ResponseEntity<?> promoteUserToAdmin(
            @PathVariable(name = "user-id") Integer userId
    ) {
        userService.promoteToAdmin(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
