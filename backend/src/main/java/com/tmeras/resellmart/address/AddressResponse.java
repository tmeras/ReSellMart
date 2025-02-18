package com.tmeras.resellmart.address;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressResponse {

    private Integer id;

    private String country;

    private String street;

    private String state;

    private String city;

    private String postalCode;

    private boolean main;

    private AddressType addressType;

    private Integer userId;
}
