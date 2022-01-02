package com.hoon.msa.composite.product.services;

import com.hoon.api.composite.product.*;
import com.hoon.api.core.product.Product;
import com.hoon.api.core.recommendation.Recommendation;
import com.hoon.api.core.review.Review;
import com.hoon.util.exceptions.NotFoundException;
import com.hoon.util.http.ServiceUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private final ServiceUtil serviceUtil;
    private ProductCompositeIntegration integration;

    @Override
    public void createCompositeProduct(ProductAggregate body) {
        try {

            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            Product product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
                    integration.createReview(review);
                });
            }

            log.debug("createCompositeProduct: composite entites created for productId: {}", body.getProductId());

        } catch (RuntimeException re) {
            log.warn("createCompositeProduct failed", re);
            throw re;
        }
    }

    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId) {
//        log.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);
//
//        Product product = integration.getProduct(productId);
//        if (product == null) throw new NotFoundException("No product found for productId: " + productId);
//
//        List<Recommendation> recommendations = integration.getRecommendations(productId);
//
//        List<Review> reviews = integration.getReviews(productId);
//
//        log.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);
//
//        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
        return Mono.zip(
            values -> createProductAggregate((Product) values[0], (List<Recommendation>) values[1], (List<Review>) values[2], serviceUtil.getServiceAddress()),
            integration.getProduct(productId),
            integration.getRecommendations(productId).collectList(),
            integration.getReviews(productId).collectList())
                .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                .log();
    }

    @Override
    public void deleteCompositeProduct(int productId) {
        log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        integration.deleteProduct(productId);

        integration.deleteRecommendations(productId);

        integration.deleteReviews(productId);

        log.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        // 1. Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null)  ? null :
                reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
