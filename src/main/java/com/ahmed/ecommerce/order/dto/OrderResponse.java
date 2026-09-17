package com.ahmed.ecommerce.order.dto;

import com.ahmed.ecommerce.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private Long id;

    private OrderStatus status;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

    private ShippingAddressResponse shippingAddress;

    private List<OrderItemResponse> items;
}