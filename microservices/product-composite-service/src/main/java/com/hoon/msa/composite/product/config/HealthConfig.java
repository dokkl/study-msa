package com.hoon.msa.composite.product.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
public class HealthConfig {
    private WebClient webClient;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Bean
    public ReactiveHealthContributor coreServices() {
        Map<String, ReactiveHealthContributor> map = new LinkedHashMap<>();
        map.put("product", (ReactiveHealthIndicator)() -> getHealth("http://product"));
        map.put("recommendation", (ReactiveHealthIndicator)() -> getHealth("http://recommendation"));
        map.put("review", (ReactiveHealthIndicator)() -> getHealth("http://review"));
//        map.put("product-composite", (ReactiveHealthIndicator)() -> getHealth("http://product-composite"));

        return CompositeReactiveHealthContributor.fromMap(map);

//        return CompositeReactiveHealthContributor.fromMap(
//                Map.of("product", productHealth
//                        , "recommendation", recommendationHealth
//                        , "review", reviewHealth)
//        );
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        log.debug("Will call the Health API on URL: {}", url);
        return getWebClient().get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }
}
