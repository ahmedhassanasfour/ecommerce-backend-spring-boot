package com.ahmed.ecommerce.address.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddressResponse {

    private Long id;

    private String fullName;

    private String phone;

    private String country;

    private String city;

    private String area;

    private String street;

    private String building;

    private String floor;

    private String apartment;

    private boolean defaultAddress;
}