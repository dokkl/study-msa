package com.hoon.msa.core.product.services;

import com.hoon.api.core.product.Product;
import com.hoon.api.core.product.ProductService;
import com.hoon.msa.core.product.persistence.ProductEntity;
import com.hoon.msa.core.product.persistence.ProductRepository;
import com.hoon.util.exceptions.InvalidInputException;
import com.hoon.util.exceptions.NotFoundException;
import com.hoon.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class ProductServiceImpl implements ProductService {

    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        log.debug("/product return the found product for productId={}", productId);

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

//        if (productId == 13) throw new NotFoundException("No product found for productId: " + productId);
//
//        return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());

//        ProductEntity entity = repository.findByProductId(productId).orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
//        Product response = mapper.entityToApi(entity);
//        response.setServiceAddress(serviceUtil.getServiceAddress());
//        return response;

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log()
                .map(entity -> mapper.entityToApi(entity))
                .map(product -> {
                    product.setServiceAddress(serviceUtil.getServiceAddress());
                    return product;
                });
    }

    @Override
    public Product createProduct(Product body) {
        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

        ProductEntity entity = mapper.apiToEntity(body);
        Mono<Product> newEntity = repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity.block();

//        try {
//            ProductEntity entity = mapper.apiToEntity(body);
//            ProductEntity newEntity = repository.save(entity);
//            return mapper.entityToApi(newEntity);
//        } catch (DuplicateKeyException dke) {
//            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
//        }
    }

    @Override
    public void deleteProduct(int productId) {
//        repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId).log().map(e -> repository.delete(e)).flatMap(e -> e).block();
    }
}
