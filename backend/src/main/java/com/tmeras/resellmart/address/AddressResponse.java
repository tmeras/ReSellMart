package com.tmeras.resellmart.address;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressResponse {

    private Integer id;

    private String country;

    private String street;

    private String state;

    private String city;

    private String postalCode;

    private boolean primary;

    private AddressType addressType;
}
