package com.ahmed.ecommerce.review.service;

import com.ahmed.ecommerce.order.entity.OrderStatus;
import com.ahmed.ecommerce.order.repository.OrderRepository;
import com.ahmed.ecommerce.product.entity.Product;
import com.ahmed.ecommerce.product.repository.ProductRepository;
import com.ahmed.ecommerce.review.dto.ReviewRequest;
import com.ahmed.ecommerce.review.dto.ReviewResponse;
import com.ahmed.ecommerce.review.entity.Review;
import com.ahmed.ecommerce.review.repository.ReviewRepository;
import com.ahmed.ecommerce.user.entity.User;
import com.ahmed.ecommerce.user.repository.UserRepository;
import com.ahmed.ecommerce.exception.BadRequestException;
import com.ahmed.ecommerce.exception.ResourceNotFoundException;
import com.ahmed.ecommerce.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;


    // =========================
    // Add Review
    // =========================

    @Transactional
    public ReviewResponse addReview(
            Long userId,
            Long productId,
            ReviewRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found")
                );

        // Check if user already reviewed product
        if (reviewRepository.existsByUserIdAndProductId(
                userId,
                productId
        )) {
            throw new BadRequestException(
                    "You already reviewed this product"
            );
        }

        // Check if user purchased product
        boolean purchased =
                orderRepository.existsDeliveredProduct(
                        userId,
                        productId,
                        OrderStatus.DELIVERED

                );

        if (!purchased) {
            throw new BadRequestException(
                    "You can review a product only after receiving it"
            );
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview =
                reviewRepository.save(review);

        return mapToResponse(savedReview);
    }


    // =========================
    // Get Product Reviews
    // =========================

    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(
            Long productId
    ) {

        // Make sure product exists
        productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found")
                );

        return reviewRepository
                .findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================
    // Delete Review
    // =========================

    @Transactional
    public void deleteReview(
            Long userId,
            Long reviewId
    ) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Review not found")
                );

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException(
                    "You are not allowed to delete this review"
            );
        }

        reviewRepository.delete(review);
    }


    // =========================
    // Mapping
    // =========================

    private ReviewResponse mapToResponse(
            Review review
    ) {

        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getFirstName()
                        + " "
                        + review.getUser().getLastName(),
                review.getProduct().getId(),
                review.getRating(),
                review.getComment()
        );
    }
}