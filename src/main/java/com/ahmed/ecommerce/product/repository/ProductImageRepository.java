package com.ahmed.ecommerce.product.repository;

import com.ahmed.ecommerce.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
