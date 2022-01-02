package com.hoon.msa.core.product.services;

import com.hoon.api.core.product.Product;
import com.hoon.api.core.product.ProductService;
import com.hoon.api.event.Event;
import com.hoon.util.exceptions.EventProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@Slf4j
@EnableBinding(Sink.class)
public class MessageProcessor {

    private final ProductService productService;

    @Autowired
    public MessageProcessor(ProductService productService) {
        this.productService = productService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Product> event) {

        log.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Product product = event.getData();
            log.info("Create product with ID: {}", product.getProductId());
            Product savedProduct = productService.createProduct(product);
            log.info("savedProduct : {}", savedProduct.toString());
            break;

        case DELETE:
            int productId = event.getKey();
            log.info("Delete recommendations with ProductID: {}", productId);
            productService.deleteProduct(productId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            log.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        log.info("Message processing done!");
    }
}
