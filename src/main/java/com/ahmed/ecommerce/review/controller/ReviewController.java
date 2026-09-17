package com.ahmed.ecommerce.review.controller;

import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.review.dto.ReviewRequest;
import com.ahmed.ecommerce.review.dto.ReviewResponse;
import com.ahmed.ecommerce.review.service.ReviewService;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;


    // =========================
    // Add Review
    // =========================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse addReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return reviewService.addReview(
                user.getId(),
                productId,
                request
        );
    }


    // =========================
    // Get Product Reviews
    // =========================

    @GetMapping
    public List<ReviewResponse> getProductReviews(
            @PathVariable Long productId
    ) {

        return reviewService.getProductReviews(
                productId
        );
    }


    // =========================
    // Delete Review
    // =========================

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        reviewService.deleteReview(
                user.getId(),
                reviewId
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