package com.ahmed.ecommerce.address.controller;

import com.ahmed.ecommerce.address.dto.AddressRequest;
import com.ahmed.ecommerce.address.dto.AddressResponse;
import com.ahmed.ecommerce.address.service.AddressService;
import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserRepository userRepository;


    // =========================
    // Create Address
    // =========================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse createAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return addressService.createAddress(
                user.getId(),
                request
        );
    }


    // =========================
    // Get My Addresses
    // =========================

    @GetMapping
    public List<AddressResponse> getMyAddresses(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return addressService.getUserAddresses(
                user.getId()
        );
    }


    // =========================
    // Get Address
    // =========================

    @GetMapping("/{addressId}")
    public AddressResponse getAddress(
            @PathVariable Long addressId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return addressService.getAddress(
                user.getId(),
                addressId
        );
    }


    // =========================
    // Update Address
    // =========================

    @PutMapping("/{addressId}")
    public AddressResponse updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return addressService.updateAddress(
                user.getId(),
                addressId,
                request
        );
    }


    // =========================
    // Delete Address
    // =========================

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        addressService.deleteAddress(
                user.getId(),
                addressId
        );
    }


    // =========================
    // Set Default Address
    // =========================

    @PatchMapping("/{addressId}/default")
    public AddressResponse setDefaultAddress(
            @PathVariable Long addressId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return addressService.setDefaultAddress(
                user.getId(),
                addressId
        );
    }


    // =========================
    // Current User
    // =========================

    private User getCurrentUser(
            Authentication authentication
    ) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );
    }
}