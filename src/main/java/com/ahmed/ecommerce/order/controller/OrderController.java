package com.ahmed.ecommerce.order.controller;

import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.order.dto.CreateOrderRequest;
import com.ahmed.ecommerce.order.dto.OrderResponse;
import com.ahmed.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.ahmed.ecommerce.order.service.OrderService;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;


    // =========================
    // Create Order
    // =========================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return orderService.createOrder(
                user.getId(),
                request
        );
    }


    // =========================
    // Get My Orders
    // =========================

    @GetMapping
    public List<OrderResponse> getMyOrders(
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return orderService.getUserOrders(user.getId());
    }


    // =========================
    // Get My Order By ID
    // =========================

    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return orderService.getOrderById(
                user.getId(),
                orderId
        );
    }


    // =========================
    // Update Order Status
    // =========================

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {

        return orderService.updateOrderStatus(
                orderId,
                request
        );
    }

    @PatchMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        User user = getCurrentUser(authentication);

        return orderService.cancelOrder(
                user.getId(),
                orderId
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