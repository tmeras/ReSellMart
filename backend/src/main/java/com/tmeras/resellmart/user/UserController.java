package com.tmeras.resellmart.user;

import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @PutMapping("/{user-id}/image")
    public ResponseEntity<?> uploadUserImage(
            @PathVariable(name = "user-id") Integer userId,
            @RequestPart("image") MultipartFile image,
            Authentication authentication
    ) throws IOException {
        userService.uploadUserImage(image, userId, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // TODO: User deletion endpoint (mind related entities)
}
