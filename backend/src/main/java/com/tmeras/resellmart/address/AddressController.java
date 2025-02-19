package com.tmeras.resellmart.address;

import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> save(
            @Valid @RequestBody AddressRequest addressRequest,
            Authentication authentication
    ) {
        AddressResponse savedAddress = addressService.save(addressRequest, authentication);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<AddressResponse>> findAll(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ADDRESSES_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR, required = false) String sortDirection
    ) {
        PageResponse<AddressResponse> foundAddresses =
                addressService.findAll(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(foundAddresses, HttpStatus.OK);
    }

    @GetMapping("/users/{user-id}")
    public ResponseEntity<List<AddressResponse>> findAllByUserId(
            @PathVariable(name = "user-id") Integer userId,
            Authentication authentication
    ) {
        List<AddressResponse> foundAddresses = addressService.findAllByUserId(userId, authentication);
        return new ResponseEntity<>(foundAddresses, HttpStatus.OK);
    }

    @GetMapping("/users/{user-id}/non-deleted")
    public ResponseEntity<List<AddressResponse>> findAllNonDeletedByUserId(
            @PathVariable(name = "user-id") Integer userId,
            Authentication authentication
    ) {
        List<AddressResponse> foundAddresses = addressService.findAllNonDeletedByUserId(userId, authentication);
        return new ResponseEntity<>(foundAddresses, HttpStatus.OK);
    }

    @PatchMapping("/{address-id}/main")
    public ResponseEntity<AddressResponse> makeMain(
            @PathVariable(name = "address-id") Integer addressId,
            Authentication authentication
    ) {
        // Make the specified address the main address of this user
        AddressResponse updatedAddress = addressService.makeMain(addressId, authentication);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @PutMapping("/{address-id}")
    public ResponseEntity<AddressResponse> update(
            @Valid @RequestBody AddressRequest addressRequest,
            @PathVariable(name = "address-id") Integer addressId,
            Authentication authentication
    ) {
        AddressResponse updatedAddress = addressService.update(addressRequest, addressId, authentication);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @DeleteMapping("/{address-id}")
    public ResponseEntity<?> delete(
            @PathVariable(name = "address-id") Integer addressId,
            Authentication authentication
    ) {
        addressService.delete(addressId, authentication);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
