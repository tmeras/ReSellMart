package com.tmeras.resellmart.user;

import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


}
