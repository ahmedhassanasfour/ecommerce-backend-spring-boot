package com.ahmed.ecommerce.product.service;

import com.ahmed.ecommerce.category.entity.Category;
import com.ahmed.ecommerce.category.repository.CategoryRepository;
import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.product.dto.ProductRequest;
import com.ahmed.ecommerce.product.dto.ProductResponse;
import com.ahmed.ecommerce.product.entity.Product;
import com.ahmed.ecommerce.product.entity.ProductImage;
import com.ahmed.ecommerce.product.repository.ProductRepository;
import com.ahmed.ecommerce.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;


    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String name,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {

        Specification<Product> specification =
                (root, query, criteriaBuilder) -> null;

        if (name != null && !name.isBlank()) {
            specification = specification.and(
                    ProductSpecification.hasName(name)
            );
        }

        if (categoryId != null) {
            specification = specification.and(
                    ProductSpecification.hasCategory(categoryId)
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    ProductSpecification.priceGreaterThanOrEqual(minPrice)
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    ProductSpecification.priceLessThanOrEqual(maxPrice)
            );
        }

        return productRepository
                .findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    @CacheEvict(value = "products", key = "'all'")
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(category)
                .images(new ArrayList<>())
                .build();

        if (request.getImageUrls() != null) {

            for (String imageUrl : request.getImageUrls()) {

                ProductImage image = ProductImage.builder()
                        .imageUrl(imageUrl)
                        .product(product)
                        .build();

                product.getImages().add(image);
            }
        }

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Cacheable(value = "products", key = "'all'")
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        return mapToResponse(product);
    }

    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", key = "'all'")
    })
    @Transactional
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request
    ) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);

        product.getImages().clear();

        if (request.getImageUrls() != null) {

            for (String imageUrl : request.getImageUrls()) {

                ProductImage image = ProductImage.builder()
                        .imageUrl(imageUrl)
                        .product(product)
                        .build();

                product.getImages().add(image);
            }
        }

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", key = "'all'")
    })
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {

        List<String> imageUrls = product.getImages()
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                imageUrls
        );
    }
}