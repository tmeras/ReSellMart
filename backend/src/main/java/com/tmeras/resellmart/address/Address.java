package com.tmeras.resellmart.address;

import com.tmeras.resellmart.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue
    private Integer id;

    private String country;

    private String street;

    private String state;

    private String city;

    private String postalCode;

    private boolean main;

    private boolean deleted;

    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
}
