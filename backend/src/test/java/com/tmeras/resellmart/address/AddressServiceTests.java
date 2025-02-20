package com.tmeras.resellmart.address;

import com.tmeras.resellmart.TestDataUtils;
import com.tmeras.resellmart.common.AppConstants;
import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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

    @Test
    public void shouldFindAllAddresses() {
        Sort sort = AppConstants.SORT_DIR.equalsIgnoreCase("asc") ?
                Sort.by(AppConstants.SORT_ADDRESSES_BY).ascending() : Sort.by(AppConstants.SORT_ADDRESSES_BY).descending();
        Pageable pageable = PageRequest.of(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT, sort);
        Page<Address> page = new PageImpl<>(List.of(addressA, addressB));

        when(addressRepository.findAll(pageable)).thenReturn(page);
        when(addressMapper.toAddressResponse(addressA)).thenReturn(addressResponseA);
        when(addressMapper.toAddressResponse(addressB)).thenReturn(addressResponseB);

        PageResponse<AddressResponse> pageResponse =
                addressService.findAll(AppConstants.PAGE_NUMBER_INT, AppConstants.PAGE_SIZE_INT,
                        AppConstants.SORT_ADDRESSES_BY, AppConstants.SORT_DIR);

        assertThat(pageResponse).isNotNull();
        assertThat(pageResponse.getContent().size()).isEqualTo(2);
        assertThat(pageResponse.getContent().get(0)).isEqualTo(addressResponseA);
        assertThat(pageResponse.getContent().get(1)).isEqualTo(addressResponseB);
    }

    @Test
    public void shouldFindAllAddressesByUserIdWhenValidRequest() {
        when(addressRepository.findAllWithAssociationsByUserId(addressA.getUser().getId()))
                .thenReturn(List.of(addressA));
        when(addressMapper.toAddressResponse(addressA)).thenReturn(addressResponseA);

        List<AddressResponse> addressResponses =
                addressService.findAllByUserId(addressA.getUser().getId(), authentication);

        assertThat(addressResponses.size()).isEqualTo(1);
        assertThat(addressResponses.get(0)).isEqualTo(addressResponseA);
    }

    @Test
    public void shouldNotFindAllAddressesByUserIdWhenAddressOwnerIsNotLoggedIn() {
        assertThatThrownBy(() -> addressService.findAllByUserId(addressB.getUser().getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to view the addresses of this user");
    }

    @Test
    public void shouldFindAllNonDeletedAddressesByUserIdWhenValidRequest() {
        when(addressRepository.findAllNonDeletedWithAssociationsByUserId(addressA.getUser().getId()))
                .thenReturn(List.of(addressA));
        when(addressMapper.toAddressResponse(addressA)).thenReturn(addressResponseA);

        List<AddressResponse> addressResponses =
                addressService.findAllNonDeletedByUserId(addressA.getUser().getId(), authentication);

        assertThat(addressResponses.size()).isEqualTo(1);
        assertThat(addressResponses.get(0)).isEqualTo(addressResponseA);
    }

    @Test
    public void shouldNotFindAllNonDeletedAddressesByUserIdWhenAddressOwnerIsNotLoggedIn() {
        assertThatThrownBy(() -> addressService.findAllNonDeletedByUserId(addressB.getUser().getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to view the addresses of this user");
    }

    @Test
    public void shouldMakeAddressMainWhenValidRequest() {
        // Define a second address belonging to user A
        Address addressC = Address.builder()
                .id(3)
                .country("United Kingdom")
                .street("Oxford Street")
                .state("England")
                .city("London")
                .postalCode("W1D 1BS")
                .main(false)
                .deleted(false)
                .addressType(AddressType.WORK)
                .user(addressA.getUser())
                .build();
        AddressResponse addressResponseC = AddressResponse.builder()
                .id(3)
                .country("United Kingdom")
                .street("Oxford Street")
                .state("England")
                .city("London")
                .postalCode("W1D 1BS")
                .main(true)
                .deleted(false)
                .addressType(AddressType.WORK)
                .userId(addressA.getUser().getId())
                .build();
        addressA.setMain(true);

        when(addressRepository.existsById(addressC.getId())).thenReturn(true);
        when(addressRepository.findAllWithAssociationsByUserId(addressA.getUser().getId()))
                .thenReturn(List.of(addressA, addressC));
        when(addressMapper.toAddressResponse(addressC)).thenReturn(addressResponseC);

        AddressResponse addressResponse = addressService.makeMain(addressC.getId(), authentication);

        assertThat(addressResponse).isEqualTo(addressResponseC);
        assertThat(addressA.isMain()).isFalse();
        assertThat(addressC.isMain()).isTrue();
    }

    @Test
    public void shouldNotMakeAddressMainWhenInvalidAddressId() {
        when(addressRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> addressService.makeMain(99, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No address found with ID: 99");
    }

    @Test
    public void shouldNotMakeAddressMainWhenAddressOwnerIsNotLoggedIn() {
        when(addressRepository.existsById(addressB.getId())).thenReturn(true);
        when(addressRepository.findAllWithAssociationsByUserId(addressA.getUser().getId()))
                .thenReturn(List.of(addressA));

        assertThatThrownBy(() -> addressService.makeMain(addressB.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("The specified address is related to another user");
    }

    @Test
    public void shouldUpdateUserWhenValidRequest() {
        addressRequestA.setCountry("Updated country");
        addressRequestA.setStreet("Updated street");
        addressResponseA.setCountry("Updated country");
        addressResponseA.setStreet("Updated street");

        when(addressRepository.findWithAssociationsById(addressA.getId())).thenReturn(Optional.of(addressA));
        when(addressRepository.save(addressA)).thenReturn(addressA);
        when(addressMapper.toAddressResponse(addressA)).thenReturn(addressResponseA);

        AddressResponse addressResponse = addressService.update(addressRequestA, addressA.getId(), authentication);

        assertThat(addressResponse).isEqualTo(addressResponseA);
        assertThat(addressA.getCountry()).isEqualTo("Updated country");
        assertThat(addressA.getStreet()).isEqualTo("Updated street");
    }

    @Test
    public void shouldNotUpdateUserWhenInvalidAddressId() {
        when(addressRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.update(addressRequestA, 99, authentication));
    }

    @Test
    public void shouldNotUpdateUserWhenNotAdminAndAddressOwnerIsNotLoggedIn() {
        authentication = new UsernamePasswordAuthenticationToken(
                addressB.getUser(),addressB.getUser().getPassword(), addressB.getUser().getAuthorities()
        );

        when(addressRepository.findWithAssociationsById(addressA.getId())).thenReturn(Optional.of(addressA));

        assertThatThrownBy(() -> addressService.update(addressRequestA, addressA.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to update the address of this user");
    }

    @Test
    public void shouldDeleteAddressWhenValidRequest() {
        when(addressRepository.findWithAssociationsById(addressA.getId())).thenReturn(Optional.of(addressA));

        addressService.delete(addressA.getId(), authentication);

        assertThat(addressA.isDeleted()).isTrue();
    }

    @Test
    public void shouldNotDeleteAddressWhenInvalidAddressId() {
        when(addressRepository.findWithAssociationsById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.delete(99, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No address found with ID: 99");
    }

    @Test
    public void shouldNotDeleteAddressWhenNotAdminAndAddressOwnerIsNotLoggedIn() {
        authentication = new UsernamePasswordAuthenticationToken(
                addressB.getUser(),addressB.getUser().getPassword(), addressB.getUser().getAuthorities()
        );

        when(addressRepository.findWithAssociationsById(addressA.getId())).thenReturn(Optional.of(addressA));

        assertThatThrownBy(() -> addressService.delete(addressA.getId(), authentication))
                .isInstanceOf(OperationNotPermittedException.class)
                .hasMessage("You do not have permission to delete the address of this user");
    }
}
