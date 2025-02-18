package com.tmeras.resellmart.address;

import com.tmeras.resellmart.common.PageResponse;
import com.tmeras.resellmart.exception.OperationNotPermittedException;
import com.tmeras.resellmart.exception.ResourceNotFoundException;
import com.tmeras.resellmart.user.User;
import com.tmeras.resellmart.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    public AddressResponse save(AddressRequest addressRequest, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // User is logged in, so already exists => just call .get() on optional to retrieve Hibernate-managed entity
        currentUser = userRepository.findById(currentUser.getId()).get();

        Address address = addressMapper.toAddress(addressRequest);
        address.setUser(currentUser);
        address.setDeleted(false);
        // TODO: Handle setting first address as main

        Address savedAddress = addressRepository.save(address);
        return addressMapper.toAddressResponse(savedAddress);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<AddressResponse> findAll(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Address> addresses = addressRepository.findAll(pageable);
        List<AddressResponse> addressResponses = addresses.stream()
                .map(addressMapper::toAddressResponse)
                .toList();

        return new PageResponse<>(
                addressResponses,
                addresses.getNumber(),
                addresses.getSize(),
                addresses.getTotalElements(),
                addresses.getTotalPages(),
                addresses.isFirst(),
                addresses.isLast()
        );
    }

    public List<AddressResponse> findAllByUserId(Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (!currentUser.getId().equals(userId))
            throw new OperationNotPermittedException("You do not have permission to view the addresses of this user");

        List<Address> addresses = addressRepository.findAllWithAssociationsByUserId(userId);

        return addresses.stream()
                .map(addressMapper::toAddressResponse)
                .toList();
    }

    public AddressResponse makeMain(Integer addressId, Integer userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if(!currentUser.getId().equals(userId))
            throw new OperationNotPermittedException("You do not have permission to update the address of this user");

        List<Address> addresses = addressRepository.findAllWithAssociationsByUserId(userId);
        for (Address address : addresses)
            address.setMain(false);

        Address specifiedAddress = addresses.stream()
                .filter(address -> address.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No address found with ID: " + addressId));
        specifiedAddress.setMain(true);

        addressRepository.saveAll(addresses);
        return addressMapper.toAddressResponse(specifiedAddress);
    }


    public AddressResponse update(AddressRequest addressRequest, Integer addressId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        boolean isCurrentUserAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        Address existingAddress = addressRepository.findWithAssociationsById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("No address found with ID: " + addressId));

        if (!isCurrentUserAdmin && !existingAddress.getUser().getId().equals(currentUser.getId()))
            throw new OperationNotPermittedException("You do not have permission to update the address of this user");

        existingAddress.setCountry(addressRequest.getCountry());
        existingAddress.setStreet(addressRequest.getStreet());
        existingAddress.setCity(addressRequest.getCity());
        existingAddress.setState(addressRequest.getState());
        existingAddress.setPostalCode(addressRequest.getPostalCode());
        existingAddress.setAddressType(AddressType.valueOf(addressRequest.getAddressType()));

        Address updatedAddress = addressRepository.save(existingAddress);
        return addressMapper.toAddressResponse(updatedAddress);
    }

    public void delete(Integer addressId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        boolean isCurrentUserAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        Address existingAddress = addressRepository.findWithAssociationsById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("No address found with ID: " + addressId));

        if (!isCurrentUserAdmin && !existingAddress.getUser().getId().equals(currentUser.getId()))
            throw new OperationNotPermittedException("You do not have permission to update the address of this user");

        // Soft delete only
        existingAddress.setDeleted(true);
        addressRepository.save(existingAddress);
    }
}
