package com.ahmed.ecommerce.wishlist.service;

import com.ahmed.ecommerce.product.entity.Product;
import com.ahmed.ecommerce.product.repository.ProductRepository;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import com.ahmed.ecommerce.wishlist.dto.WishlistResponse;
import com.ahmed.ecommerce.wishlist.entity.WishlistItem;
import com.ahmed.ecommerce.wishlist.repository.WishlistRepository;
import com.ahmed.ecommerce.exception.BadRequestException;
import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    // =========================
    // Add Product
    // =========================

    @Transactional
    public WishlistResponse addProduct(
            Long userId,
            Long productId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found")
                );

        if (wishlistRepository.existsByUserIdAndProductId(
                userId,
                productId
        )) {
            throw new BadRequestException(
                    "Product already exists in wishlist"
            );
        }

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        WishlistItem savedItem =
                wishlistRepository.save(item);

        return mapToResponse(savedItem);
    }


    // =========================
    // Get Wishlist
    // =========================

    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlist(
            Long userId
    ) {

        return wishlistRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================
    // Remove Product
    // =========================

    @Transactional
    public void removeProduct(
            Long userId,
            Long productId
    ) {

        WishlistItem item =
                wishlistRepository
                        .findByUserIdAndProductId(
                                userId,
                                productId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found in wishlist"
                                )
                        );

        wishlistRepository.delete(item);
    }


    // =========================
    // Mapping
    // =========================

    private WishlistResponse mapToResponse(
            WishlistItem item
    ) {

        Product product = item.getProduct();

        return new WishlistResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getPrice()
        );
    }
}