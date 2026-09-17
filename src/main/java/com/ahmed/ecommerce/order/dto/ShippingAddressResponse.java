package com.ahmed.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShippingAddressResponse {

    private String fullName;
    private String phone;
    private String country;
    private String city;
    private String area;
    private String street;
    private String building;
    private String floor;
    private String apartment;
}