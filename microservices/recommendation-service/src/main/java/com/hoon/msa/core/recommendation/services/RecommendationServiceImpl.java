package com.hoon.msa.core.recommendation.services;

import com.hoon.api.core.recommendation.Recommendation;
import com.hoon.api.core.recommendation.RecommendationService;
import com.hoon.msa.core.recommendation.persistence.RecommendationEntity;
import com.hoon.msa.core.recommendation.persistence.RecommendationRepository;
import com.hoon.util.exceptions.InvalidInputException;
import com.hoon.util.http.ServiceUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
//        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
//
//        if (productId == 113) {
//            log.debug("No recommendations found for productId: {}", productId);
//            return  new ArrayList<>();
//        }
//
//        List<Recommendation> list = new ArrayList<>();
//        list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
//        list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
//        list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));
//
//        log.debug("/recommendation response size: {}", list.size());
//
//        return list;

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
//        try {
//            RecommendationEntity entity = mapper.apiToEntity(body);
//            RecommendationEntity newEntity = repository.save(entity);
//
//            log.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
//            return mapper.entityToApi(newEntity);
//
//        } catch (DuplicateKeyException dke) {
//            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
//        }
        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

        RecommendationEntity entity = mapper.apiToEntity(body);
        Mono<Recommendation> newEntity = repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity.block();
    }

    @Override
    public void deleteRecommendations(int productId) {
//        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
//        repository.deleteAll(repository.findByProductId(productId));
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId)).block();
    }

}
