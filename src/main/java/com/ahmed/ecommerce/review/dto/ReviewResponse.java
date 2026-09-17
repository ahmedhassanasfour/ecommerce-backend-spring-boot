package com.ahmed.ecommerce.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewResponse {

    private Long id;

    private Long userId;

    private String userName;

    private Long productId;

    private Integer rating;

    private String comment;
}