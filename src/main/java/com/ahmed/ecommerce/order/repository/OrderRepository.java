package com.ahmed.ecommerce.order.repository;

import com.ahmed.ecommerce.order.entity.Order;
import com.ahmed.ecommerce.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
            SELECT COUNT(o) > 0
            FROM Order o
            JOIN o.items item
            WHERE o.user.id = :userId
            AND item.product.id = :productId
            AND o.status = :status
            """)
    boolean existsDeliveredProduct(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("status") OrderStatus status
    );
}