package com.tmeras.resellmart.address;

import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


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



}
