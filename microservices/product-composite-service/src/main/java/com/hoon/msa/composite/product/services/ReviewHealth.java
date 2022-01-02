package com.hoon.msa.composite.product.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReviewHealth implements ReactiveHealthIndicator {
    @Autowired
    ProductCompositeIntegration integration;

    @Override
    public Mono<Health> health() {
        return integration.getReviewHealth();
    }
}
