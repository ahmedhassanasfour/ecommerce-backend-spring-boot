package com.ahmed.ecommerce.address.service;

import com.ahmed.ecommerce.address.dto.AddressRequest;
import com.ahmed.ecommerce.address.dto.AddressResponse;
import com.ahmed.ecommerce.address.entity.Address;
import com.ahmed.ecommerce.address.repository.AddressRepository;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // =========================
    // Create Address
    // =========================

    @Transactional
    public AddressResponse createAddress(
            Long userId,
            AddressRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        // If this address should be default
        if (request.isDefaultAddress()) {
            removeDefaultAddress(userId);
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .country(request.getCountry())
                .city(request.getCity())
                .area(request.getArea())
                .street(request.getStreet())
                .building(request.getBuilding())
                .floor(request.getFloor())
                .apartment(request.getApartment())
                .defaultAddress(request.isDefaultAddress())
                .build();

        Address savedAddress = addressRepository.save(address);

        return mapToResponse(savedAddress);
    }


    // =========================
    // Get My Addresses
    // =========================

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {

        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================
    // Get Address By ID
    // =========================

    @Transactional(readOnly = true)
    public AddressResponse getAddress(
            Long userId,
            Long addressId
    ) {

        Address address = getUserAddress(
                userId,
                addressId
        );

        return mapToResponse(address);
    }


    // =========================
    // Update Address
    // =========================

    @Transactional
    public AddressResponse updateAddress(
            Long userId,
            Long addressId,
            AddressRequest request
    ) {

        Address address = getUserAddress(
                userId,
                addressId
        );

        if (request.isDefaultAddress()) {
            removeDefaultAddress(userId);
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setCountry(request.getCountry());
        address.setCity(request.getCity());
        address.setArea(request.getArea());
        address.setStreet(request.getStreet());
        address.setBuilding(request.getBuilding());
        address.setFloor(request.getFloor());
        address.setApartment(request.getApartment());
        address.setDefaultAddress(request.isDefaultAddress());

        return mapToResponse(address);
    }


    // =========================
    // Delete Address
    // =========================

    @Transactional
    public void deleteAddress(
            Long userId,
            Long addressId
    ) {

        Address address = getUserAddress(
                userId,
                addressId
        );

        addressRepository.delete(address);
    }


    // =========================
    // Set Default Address
    // =========================

    @Transactional
    public AddressResponse setDefaultAddress(
            Long userId,
            Long addressId
    ) {

        Address address = getUserAddress(
                userId,
                addressId
        );

        removeDefaultAddress(userId);

        address.setDefaultAddress(true);

        return mapToResponse(address);
    }


    // =========================
    // Remove Existing Default
    // =========================

    private void removeDefaultAddress(Long userId) {

        addressRepository
                .findByUserIdAndDefaultAddressTrue(userId)
                .ifPresent(address ->
                        address.setDefaultAddress(false)
                );
    }


    // =========================
    // Get User Address
    // =========================

    private Address getUserAddress(
            Long userId,
            Long addressId
    ) {

        return addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Address not found"
                        )
                );
    }


    // =========================
    // Mapping
    // =========================

    private AddressResponse mapToResponse(
            Address address
    ) {

        return new AddressResponse(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getCountry(),
                address.getCity(),
                address.getArea(),
                address.getStreet(),
                address.getBuilding(),
                address.getFloor(),
                address.getApartment(),
                address.isDefaultAddress()
        );
    }
}