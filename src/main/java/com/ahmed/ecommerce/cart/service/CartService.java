package com.ahmed.ecommerce.cart.service;

import com.ahmed.ecommerce.cart.dto.AddToCartRequest;
import com.ahmed.ecommerce.cart.dto.CartItemResponse;
import com.ahmed.ecommerce.cart.dto.CartResponse;
import com.ahmed.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ahmed.ecommerce.cart.entity.Cart;
import com.ahmed.ecommerce.cart.entity.CartItem;
import com.ahmed.ecommerce.cart.repository.CartItemRepository;
import com.ahmed.ecommerce.cart.repository.CartRepository;
import com.ahmed.ecommerce.product.entity.Product;
import com.ahmed.ecommerce.product.repository.ProductRepository;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import com.ahmed.ecommerce.exception.BadRequestException;
import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    // =========================
    // Add Product To Cart
    // =========================

    @Transactional
    public CartResponse addToCart(
            Long userId,
            AddToCartRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found")
                );

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(newCart);
                });

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        product.getId()
                )
                .orElse(null);

        int newQuantity;

        if (cartItem != null) {

            newQuantity = cartItem.getQuantity()
                    + request.getQuantity();

            cartItem.setQuantity(newQuantity);

        } else {

            newQuantity = request.getQuantity();

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(newQuantity)
                    .build();

            cart.getItems().add(cartItem);
        }

        if (newQuantity > product.getStockQuantity()) {
            throw new BadRequestException("Not enough stock");
        }

        cartItemRepository.save(cartItem);

        return mapToResponse(cart);
    }


    // =========================
    // Get Cart
    // =========================

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found")
                );

        return mapToResponse(cart);
    }


    // =========================
    // Update Cart Item
    // =========================

    @Transactional
    public CartResponse updateCartItem(
            Long userId,
            Long cartItemId,
            UpdateCartItemRequest request
    ) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found")
                );

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart item not found")
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Cart item does not belong to this cart");
        }

        Product product = cartItem.getProduct();

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new BadRequestException("Not enough stock");
        }

        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);

        return mapToResponse(cart);
    }


    // =========================
    // Remove Cart Item
    // =========================

    @Transactional
    public CartResponse removeCartItem(
            Long userId,
            Long cartItemId
    ) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found")
                );

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart item not found")
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Cart item does not belong to this cart");
        }

        cart.getItems().remove(cartItem);

        cartItemRepository.delete(cartItem);

        return mapToResponse(cart);
    }


    // =========================
    // Clear Cart
    // =========================

    @Transactional
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found")
                );

        cart.getItems().clear();

        cartRepository.save(cart);
    }


    // =========================
    // Mapping
    // =========================

    private CartResponse mapToResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::mapToItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        return new CartResponse(
                cart.getId(),
                items,
                total
        );
    }


    private CartItemResponse mapToItemResponse(
            CartItem cartItem
    ) {

        Product product = cartItem.getProduct();

        BigDecimal subtotal = product.getPrice()
                .multiply(
                        BigDecimal.valueOf(cartItem.getQuantity())
                );

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                subtotal
        );
    }
}