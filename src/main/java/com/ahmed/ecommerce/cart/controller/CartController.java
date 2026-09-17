package com.ahmed.ecommerce.cart.controller;

import com.ahmed.ecommerce.cart.dto.AddToCartRequest;
import com.ahmed.ecommerce.cart.dto.CartResponse;
import com.ahmed.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ahmed.ecommerce.cart.service.CartService;
import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;


    // =========================
    // Add Product To Cart
    // =========================

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return cartService.addToCart(
                user.getId(),
                request
        );
    }


    // =========================
    // Get Cart
    // =========================

    @GetMapping
    public CartResponse getCart(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return cartService.getCart(user.getId());
    }


    // =========================
    // Update Cart Item
    // =========================

    @PutMapping("/items/{cartItemId}")
    public CartResponse updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return cartService.updateCartItem(
                user.getId(),
                cartItemId,
                request
        );
    }


    // =========================
    // Remove Cart Item
    // =========================

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeCartItem(
            @PathVariable Long cartItemId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return cartService.removeCartItem(
                user.getId(),
                cartItemId
        );
    }


    // =========================
    // Clear Cart
    // =========================

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        cartService.clearCart(user.getId());
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