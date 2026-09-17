package com.ahmed.ecommerce.category.repository;

import com.ahmed.ecommerce.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category , Long> {
    boolean existsByName(String name);
}
