package com.tmeras.resellmart.address;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.role.Role;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTests {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private Address addressA;
    private Address addressB;
    private AddressRequest addressRequestA;
    private AddressResponse addressResponseA;
    private AddressResponse addressResponseB;
    private Authentication authentication;


    @BeforeEach
    public void setUp() {
        // Initialise test objects
        Role adminRole = new Role(1, "ADMIN");
        Role userRole = new Role(2, "USER");
        User userA = TestDataUtils.createUserA(Set.of(adminRole));
        User userB = TestDataUtils.createUserB(Set.of(userRole));

        addressA = TestDataUtils.createAddressA(userA);
        addressB = TestDataUtils.createAddressB(userB);
        addressRequestA = TestDataUtils.createAddressRequestA();
        addressResponseA = TestDataUtils.createAddressResponseA(userA.getId());
        addressResponseB = TestDataUtils.createAddressResponseB(userB.getId());

        authentication = new UsernamePasswordAuthenticationToken(
                userA, userA.getPassword(), userA.getAuthorities()
        );
    }

    @Test
    public void shouldSaveAddress() {
        when(userRepository.findById(addressA.getUser().getId())).thenReturn(Optional.of(addressA.getUser()));
        when(addressMapper.toAddress(addressRequestA)).thenReturn(addressA);
        when(addressRepository.findAllNonDeletedWithAssociationsByUserId(addressA.getUser().getId()))
                .thenReturn(List.of());
        when(addressRepository.save(addressA)).thenReturn(addressA);
        when(addressMapper.toAddressResponse(addressA)).thenReturn(addressResponseA);

        AddressResponse addressResponse = addressService.save(addressRequestA, authentication);

        assertThat(addressResponse).isEqualTo(addressResponseA);
        assertThat(addressA.isMain()).isTrue();
    }

}
