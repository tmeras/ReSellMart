package com.tmeras.resellmart.user;

import com.tmeras.resellmart.cart.CartItemRequest;
import com.tmeras.resellmart.cart.CartItemResponse;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
    ) {
        PageResponse<UserResponse> foundUsers = userService.findAll(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(foundUsers, HttpStatus.OK);
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

    // TODO: Return user response
    @PutMapping("/{user-id}/image")
    public ResponseEntity<?> uploadUserImage(
            @PathVariable(name = "user-id") Integer userId,
            @RequestPart("image") MultipartFile image,
            Authentication authentication
    ) throws IOException {
        userService.uploadUserImage(image, userId, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
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



    // TODO: User deletion endpoint (soft delete with marking products as unavailable)
}
