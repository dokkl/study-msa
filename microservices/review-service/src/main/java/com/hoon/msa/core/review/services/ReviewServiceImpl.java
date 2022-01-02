package com.hoon.msa.core.review.services;

import com.hoon.api.core.review.Review;
import com.hoon.api.core.review.ReviewService;
import com.hoon.msa.core.review.persistence.ReviewEntity;
import com.hoon.msa.core.review.persistence.ReviewRepository;
import com.hoon.util.exceptions.InvalidInputException;
import com.hoon.util.http.ServiceUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.function.Supplier;

import static java.util.logging.Level.FINE;

@Slf4j
@AllArgsConstructor
@RestController
public class ReviewServiceImpl implements ReviewService {
    private final Scheduler scheduler;
    private final ServiceUtil serviceUtil;
    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    @Override
    public Flux<Review> getReviews(int productId) {
//        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
//
//        if (productId == 213) {
//            log.debug("No reviews found for productId: {}", productId);
//            return  new ArrayList<>();
//        }
//
//        List<Review> list = new ArrayList<>();
//        list.add(new Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()));
//        list.add(new Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()));
//        list.add(new Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));
//
//        log.debug("/reviews response size: {}", list.size());
//        return list;

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        log.info("Will get reviews for product with id={}", productId);

        return asyncFlux(() -> Flux.fromIterable(getByProductId(productId))).log(null, FINE);

    }

    protected List<Review> getByProductId(int productId) {

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("getReviews: response size: {}", list.size());

        return list;
    }

    private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }

    @Override
    public Review createReview(Review body) {
        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            log.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
        }
    }


    @Override
    public void deleteReviews(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
