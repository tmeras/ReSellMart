package com.tmeras.resellmart.address;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressMapper {

    public Address toAddress(AddressRequest addressRequest) {
        return Address.builder()
                .country(addressRequest.getCountry())
                .street(addressRequest.getStreet())
                .state(addressRequest.getState())
                .city(addressRequest.getCity())
                .postalCode(addressRequest.getPostalCode())
                .main(addressRequest.isMain())
                .active(addressRequest.isActive())
                .addressType(AddressType.valueOf(addressRequest.getAddressType()))
                .build();
    }

    public AddressResponse toAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .country(address.getCountry())
                .street(address.getStreet())
                .state(address.getState())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .main(address.isMain())
                .active(address.isActive())
                .addressType(address.getAddressType())
                .userId(address.getUser().getId())
                .build();
    }
}
