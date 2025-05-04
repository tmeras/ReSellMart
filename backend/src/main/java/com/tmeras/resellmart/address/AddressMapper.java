package com.tmeras.resellmart.address;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressMapper {

    public Address toAddress(AddressRequest addressRequest) {
        return Address.builder()
                .name(addressRequest.getName())
                .country(addressRequest.getCountry())
                .street(addressRequest.getStreet())
                .state(addressRequest.getState())
                .city(addressRequest.getCity())
                .postalCode(addressRequest.getPostalCode())
                .phoneNumber(addressRequest.getPhoneNumber())
                .isMain(addressRequest.getIsMain())
                .addressType(AddressType.valueOf(addressRequest.getAddressType()))
                .build();
    }

    public AddressResponse toAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .name(address.getName())
                .country(address.getCountry())
                .street(address.getStreet())
                .state(address.getState())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .phoneNumber(address.getPhoneNumber())
                .isMain(address.getIsMain())
                .addressType(address.getAddressType())
                .userId(address.getUser().getId())
                .build();
    }
}
