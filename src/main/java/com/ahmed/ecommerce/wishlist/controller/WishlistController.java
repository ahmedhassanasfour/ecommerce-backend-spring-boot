package com.ahmed.ecommerce.wishlist.controller;

import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import com.ahmed.ecommerce.wishlist.dto.WishlistResponse;
import com.ahmed.ecommerce.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;


    // =========================
    // Add Product
    // =========================

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistResponse addProduct(
            @PathVariable Long productId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return wishlistService.addProduct(
                user.getId(),
                productId
        );
    }


    // =========================
    // Get Wishlist
    // =========================

    @GetMapping
    public List<WishlistResponse> getWishlist(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return wishlistService.getWishlist(
                user.getId()
        );
    }


    // =========================
    // Remove Product
    // =========================

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeProduct(
            @PathVariable Long productId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        wishlistService.removeProduct(
                user.getId(),
                productId
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