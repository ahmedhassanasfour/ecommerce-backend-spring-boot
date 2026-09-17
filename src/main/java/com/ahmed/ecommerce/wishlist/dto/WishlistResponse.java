package com.ahmed.ecommerce.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WishlistResponse {

    private Long id;

    private Long productId;

    private String productName;

    private BigDecimal price;
}