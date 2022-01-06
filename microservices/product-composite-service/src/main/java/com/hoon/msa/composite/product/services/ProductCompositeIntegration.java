package com.hoon.msa.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoon.api.core.product.Product;
import com.hoon.api.core.product.ProductService;
import com.hoon.api.core.recommendation.Recommendation;
import com.hoon.api.core.recommendation.RecommendationService;
import com.hoon.api.core.review.Review;
import com.hoon.api.core.review.ReviewService;
import com.hoon.api.event.Event;
import com.hoon.util.exceptions.InvalidInputException;
import com.hoon.util.exceptions.NotFoundException;
import com.hoon.util.http.HttpErrorInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.hoon.api.event.Event.Type.CREATE;
import static com.hoon.api.event.Event.Type.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static reactor.core.publisher.Flux.empty;

@Slf4j
@EnableBinding(ProductCompositeIntegration.MessageSources.class)
@Component
public class ProductCompositeIntegration  implements ProductService, RecommendationService, ReviewService {
//    private final RestTemplate restTemplate;
    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper mapper;

//    private final String productServiceUrl;
//    private final String recommendationServiceUrl;
//    private final String reviewServiceUrl;
    private final String productServiceUrl = "http://product";
    private final String recommendationServiceUrl = "http://recommendation";
    private final String reviewServiceUrl = "http://review";

    private MessageSources messageSources;
    private final int productServiceTimeoutSec;

    public interface MessageSources {

        String OUTPUT_PRODUCTS = "output-products";
        String OUTPUT_RECOMMENDATIONS = "output-recommendations";
        String OUTPUT_REVIEWS = "output-reviews";

        @Output(OUTPUT_PRODUCTS)
        MessageChannel outputProducts();

        @Output(OUTPUT_RECOMMENDATIONS)
        MessageChannel outputRecommendations();

        @Output(OUTPUT_REVIEWS)
        MessageChannel outputReviews();
    }

    @Autowired
    public ProductCompositeIntegration(WebClient.Builder webClientBuilder,
                                       ObjectMapper mapper,
                                       MessageSources messageSources,
                                       @Value("${app.product-service.timeoutSec}") int productServiceTimeoutSec
//                                       @Value("${app.product-service.host}") String productServiceHost,
//                                       @Value("${app.product-service.port}") int productServicePort,
//                                       @Value("${app.recommendation-service.host}") String recommendationServiceHost,
//                                       @Value("${app.recommendation-service.port}") int recommendationServicePort,
//                                       @Value("${app.review-service.host}") String reviewServiceHost,
//                                       @Value("${app.review-service.port}") int reviewServicePort
                                       ) {
//        this.restTemplate = restTemplate;
//        this.webClient = webClient.build();
        this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        this.messageSources = messageSources;
        this.productServiceTimeoutSec = productServiceTimeoutSec;
//        this.productServiceUrl        = "http://" + productServiceHost + ":" + productServicePort + "/product/";
//        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
//        this.reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";

//        productServiceUrl        = "http://" + productServiceHost + ":" + productServicePort;
//        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
//        reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort;
    }

    @Override
    public Product createProduct(Product body) {

//        try {
//            String url = productServiceUrl;
//            log.debug("Will post a new product to URL: {}", url);
//
//            Product product = restTemplate.postForObject(url, body, Product.class);
//            log.debug("Created a product with id: {}", product.getProductId());
//
//            return product;
//
//        } catch (HttpClientErrorException ex) {
//            throw handleHttpClientException(ex);
//        }
        messageSources.outputProducts()
                .send(MessageBuilder.withPayload(
                        new Event(CREATE, body.getProductId(), body)
                ).build());
        return body;
    }

    @Override
    public void deleteProduct(int productId) {
//        try {
//            String url = productServiceUrl + "/" + productId;
//            log.debug("Will call the deleteProduct API on URL: {}", url);
//
//            restTemplate.delete(url);
//
//        } catch (HttpClientErrorException ex) {
//            throw handleHttpClientException(ex);
//        }
        messageSources.outputProducts()
                .send(MessageBuilder.withPayload(
                        new Event(DELETE, productId, null)
                ).build());
    }

    @Retry(name = "product")
    @CircuitBreaker(name = "product")
    @Override
    public Mono<Product> getProduct(int productId, int delay, int faultPercent) {
//        try {
//            String url = productServiceUrl + productId;
//            log.debug("Will call getProduct API on URL: {}", url);
//
//            Product product = restTemplate.getForObject(url, Product.class);
//            log.debug("Found a product with id: {}", product.getProductId());
//            return product;
//        } catch (HttpClientErrorException ex) {
//            switch (ex.getStatusCode()) {
//                case NOT_FOUND:
//                    throw new NotFoundException(getErrorMessage(ex));
//                case UNPROCESSABLE_ENTITY:
//                    throw new InvalidInputException(getErrorMessage(ex));
//                default:
//                    log.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
//                    log.warn("Error body: {}", ex.getResponseBodyAsString());
//                    throw ex;
//            }
//        }
//        String url = productServiceUrl + "/product/" + productId;
        URI url = UriComponentsBuilder.fromUriString(productServiceUrl + "/product/{productId}?delay={delay}&faultPercent={falutPercent}")
                .build(productId, delay, faultPercent);
        return getWebClient().get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleException)
                .timeout(Duration.ofSeconds(productServiceTimeoutSec));

    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
//        try {
//            String url = recommendationServiceUrl + productId;
//
//            log.debug("Will call getRecommendations API on URL: {}", url);
//            List<Recommendation> recommendations = restTemplate.exchange(url, GET,
//                    null,
//                    new ParameterizedTypeReference<List<Recommendation>>() {}).getBody();
//            return recommendations;
//        } catch (Exception ex) {
//            log.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
//            return new ArrayList<>();
//        }

        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;

        log.debug("Will call the getRecommendations API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Recommendation.class).log().onErrorResume(error -> empty());
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
//        try {
//            String url = recommendationServiceUrl;
//            log.debug("Will post a new recommendation to URL: {}", url);
//
//            Recommendation recommendation = restTemplate.postForObject(url, body, Recommendation.class);
//            log.debug("Created a recommendation with id: {}", recommendation.getProductId());
//
//            return recommendation;
//
//        } catch (HttpClientErrorException ex) {
//            throw handleHttpClientException(ex);
//        }
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public void deleteRecommendations(int productId) {
//        try {
//            String url = recommendationServiceUrl + "?productId=" + productId;
//            log.debug("Will call the deleteRecommendations API on URL: {}", url);
//
//            restTemplate.delete(url);
//
//        } catch (HttpClientErrorException ex) {
//            throw handleHttpClientException(ex);
//        }
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (ex.getStatusCode()) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));

            case UNPROCESSABLE_ENTITY :
                return new InvalidInputException(getErrorMessage(ex));

            default:
                log.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                log.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
        }
    }

    @Override
    public Flux<Review> getReviews(int productId) {
//        try {
//            String url = reviewServiceUrl + productId;
//
//            log.debug("Will call getReviews API on URL: {}", url);
//            List<Review> reviews = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {}).getBody();
//
//            log.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
//            return reviews;
//
//        } catch (Exception ex) {
//            log.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
//            return new ArrayList<>();
//        }
        String url = reviewServiceUrl + "/review?productId=" + productId;

        log.debug("Will call the getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log()
                .onErrorResume(error -> empty());
    }

    @Override
    public Review createReview(Review body) {
//        try {
//            String url = reviewServiceUrl;
//            log.debug("Will post a new review to URL: {}", url);
//
//            Review review = restTemplate.postForObject(url, body, Review.class);
//            log.debug("Created a review with id: {}", review.getProductId());
//
//            return review;
//
//        } catch (HttpClientErrorException ex) {
//            throw handleHttpClientException(ex);
//        }

        messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public void deleteReviews(int productId) {
//        try {
//            String url = reviewServiceUrl + "?productId=" + productId;
//            log.debug("Will call the deleteReviews API on URL: {}", url);
//
//            restTemplate.delete(url);
//
//        } catch (HttpClientErrorException ex) {
//            throw handleHttpClientException(ex);
//        }
        messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(DELETE, productId, null)).build());
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (wcre.getStatusCode()) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY :
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                log.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

//    public Mono<Health> getProductHealth() {
//        return getHealth(productServiceUrl);
//    }
//
//    public Mono<Health> getRecommendationHealth() {
//        return getHealth(recommendationServiceUrl);
//    }
//
//    public Mono<Health> getReviewHealth() {
//        return getHealth(reviewServiceUrl);
//    }
//
//    private Mono<Health> getHealth(String url) {
//        url += "/actuator/health";
//        log.debug("Will call the Health API on URL: {}", url);
//        return getWebClient().get().uri(url).retrieve().bodyToMono(String.class)
//                .map(s -> new Health.Builder().up().build())
//                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
//                .log();
//    }

}
