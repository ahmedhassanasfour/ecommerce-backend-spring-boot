package com.ahmed.ecommerce.order.service;

import com.ahmed.ecommerce.cart.entity.Cart;
import com.ahmed.ecommerce.cart.entity.CartItem;
import com.ahmed.ecommerce.cart.repository.CartRepository;
import com.ahmed.ecommerce.order.dto.OrderItemResponse;
import com.ahmed.ecommerce.order.dto.OrderResponse;
import com.ahmed.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.ahmed.ecommerce.order.entity.Order;
import com.ahmed.ecommerce.order.entity.OrderItem;
import com.ahmed.ecommerce.order.entity.OrderStatus;
import com.ahmed.ecommerce.order.repository.OrderRepository;
import com.ahmed.ecommerce.product.entity.Product;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import com.ahmed.ecommerce.exception.BadRequestException;
import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.ahmed.ecommerce.address.entity.Address;
import com.ahmed.ecommerce.address.repository.AddressRepository;
import com.ahmed.ecommerce.order.dto.CreateOrderRequest;
import com.ahmed.ecommerce.order.dto.ShippingAddressResponse;
import com.ahmed.ecommerce.order.entity.ShippingAddress;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;


    // =========================
    // Create Order From Cart
    // =========================
    @Transactional
    public OrderResponse createOrder(
            Long userId,
            CreateOrderRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found")
                );

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressRepository
                .findByIdAndUserId(
                        request.getAddressId(),
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Address not found")
                );

        ShippingAddress shippingAddress = ShippingAddress.builder()
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .country(address.getCountry())
                .city(address.getCity())
                .area(address.getArea())
                .street(address.getStreet())
                .building(address.getBuilding())
                .floor(address.getFloor())
                .apartment(address.getApartment())
                .build();

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .shippingAddress(shippingAddress)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException(
                        "Not enough stock for product: "
                                + product.getName()
                );
            }

            BigDecimal price = product.getPrice();

            BigDecimal subtotal = price.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(price)
                    .build();

            order.getItems().add(orderItem);

            totalPrice = totalPrice.add(subtotal);

            product.setStockQuantity(
                    product.getStockQuantity()
                            - cartItem.getQuantity()
            );
        }

        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();

        cartRepository.save(cart);

        return mapToResponse(savedOrder);
    }


    // =========================
    // Get User Orders
    // =========================

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================
    // Get Order By ID
    // =========================

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            Long userId,
            Long orderId
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );

        // User can only see his own order
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(
                    "You are not allowed to access this order"
            );
        }

        return mapToResponse(order);
    }


    // =========================
    // Update Order Status
    // =========================

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            UpdateOrderStatusRequest request
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new BadRequestException(
                    "Invalid order status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }


        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);

        return mapToResponse(order);
    }


    @Transactional
    public OrderResponse cancelOrder(
            Long userId,
            Long orderId
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );

        // Make sure the order belongs to the current user
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(
                    "You are not allowed to cancel this order"
            );
        }

        // Can only cancel PENDING orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Order cannot be cancelled in status: "
                            + order.getStatus()
            );
        }

        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);

        return mapToResponse(order);
    }


    // =========================
    // Order Status Rules
    // =========================

    private boolean isValidTransition(
            OrderStatus current,
            OrderStatus next
    ) {

        return switch (current) {

            case PENDING ->
                    next == OrderStatus.CONFIRMED
                            || next == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    next == OrderStatus.SHIPPED
                            || next == OrderStatus.CANCELLED;

            case SHIPPED ->
                    next == OrderStatus.DELIVERED;

            case DELIVERED, CANCELLED ->
                    false;
        };
    }


    // =========================
    // Mapping
    // =========================

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::mapToItemResponse)
                .toList();

        ShippingAddressResponse shippingAddress =
                mapToShippingAddressResponse(
                        order.getShippingAddress()
                );

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                shippingAddress,
                items
        );
    }


    private OrderItemResponse mapToItemResponse(
            OrderItem orderItem
    ) {

        BigDecimal subtotal = orderItem.getPrice()
                .multiply(
                        BigDecimal.valueOf(orderItem.getQuantity())
                );

        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getPrice(),
                orderItem.getQuantity(),
                subtotal
        );
    }


    private ShippingAddressResponse mapToShippingAddressResponse(
            ShippingAddress address
    ) {

        return new ShippingAddressResponse(
                address.getFullName(),
                address.getPhone(),
                address.getCountry(),
                address.getCity(),
                address.getArea(),
                address.getStreet(),
                address.getBuilding(),
                address.getFloor(),
                address.getApartment()
        );
    }

    private void restoreStock(Order order) {

        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity()
                            + item.getQuantity()
            );
        }
    }
}