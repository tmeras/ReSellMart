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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name; // Name on address

    private String country;

    private String street;

    private String state;

    private String city;

    private String postalCode;

    private String phoneNumber;

    private Boolean isMain;

    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    public String getFullAddress() {
        if (phoneNumber == null || phoneNumber.isEmpty())
            return String.format("%s, %s, %s, %s, %s, %s", name, street, state, city, postalCode, country);

        return String.format("%s, %s, %s, %s, %s, %s, %s", name, street, state, city, postalCode, country, phoneNumber);
    }
}
