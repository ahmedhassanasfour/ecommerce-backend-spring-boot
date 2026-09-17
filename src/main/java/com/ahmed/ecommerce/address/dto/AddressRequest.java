package com.ahmed.ecommerce.address.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    @NotBlank
    private String area;

    @NotBlank
    private String street;

    @NotBlank
    private String building;

    private String floor;

    private String apartment;

    private boolean defaultAddress;
}