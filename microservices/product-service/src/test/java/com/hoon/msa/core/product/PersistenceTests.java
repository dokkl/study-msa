package com.hoon.msa.core.product;

import com.hoon.msa.core.product.persistence.ProductEntity;
import com.hoon.msa.core.product.persistence.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertEquals;

@Slf4j
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@DataMongoTest
public class PersistenceTests {
    @Autowired
    private ProductRepository repository;
    private ProductEntity savedEntity;

    @BeforeEach
    public void setupDb() {
//        repository.deleteAll();

        StepVerifier.create(repository.deleteAll()).verifyComplete();
        ProductEntity entity = new ProductEntity(1, "n", 1);

//        논블럭킹 처리방식 검증
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    return areProductEqual(entity, savedEntity);
                }).verifyComplete();

//        블록킹 처리방식 검증
//        savedEntity = repository.save(entity);
//        assertEqualsProduct(entity, savedEntity);
    }

    @Test
    public void create() {
        ProductEntity newEntity = new ProductEntity(2, "n", 2);
        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity -> {
                    return newEntity.getProductId() == createdEntity.getProductId();
                })
                .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();

//        블록킹 처리방식 검증
//        savedEntity = repository.save(newEntity);
//        ProductEntity foundEntity = repository.findById(newEntity.getId()).block();
//        assertEqualsProduct(newEntity, foundEntity);
//        assertEquals(2, repository.count());
        log.info("TEST OK create()");
    }

    @Test
    public void update() {
        savedEntity.setName("n2");
        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
                .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity -> {
                    return foundEntity.getVersion() == 1 && foundEntity.getName().equals("n2");
                });
//        블록킹 처리방식 검증
//        ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
//        assertEquals(1, (long)foundEntity.getVersion());
//        assertEquals("n2", foundEntity.getName());

        log.info("TEST OK update()");
    }

    @Test
    public void delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId()))
                        .expectNext(false).verifyComplete();

        //        assertFalse(repository.existsById(savedEntity.getId()));

        log.info("TEST OK delete()");

    }

    @Test
    public void getByProductId() {
        StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
                .verifyComplete();

//        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());
//        assertTrue(entity.isPresent());
//        assertEqualsProduct(savedEntity, entity.get());
    }

//    @Test
//    public void duplicationError() {
//        DuplicateKeyException thrown = assertThrows(DuplicateKeyException.class, () -> {
//            ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
//            ProductEntity newSave = repository.save(entity);
//            log.info("save ok savedEntity.getProductId() : {}", savedEntity.getProductId());
//
//            long count = repository.count();
//            log.info("count : {}", count);
//
//            Iterable<ProductEntity> all = repository.findAll();
//            all.forEach(entity1 -> {
//                log.info("entity1 : {}", entity1.getProductId());
//            });
//        });
//
//        log.info("thrown message : {}", thrown.getMessage());
//
////        Assertions.assertEquals("some message", thrown.getMessage());
//    }

//    @Test
//    public void paging() {
//
//        repository.deleteAll();
//
//        List<ProductEntity> newProducts = rangeClosed(1001, 1010)
//                .mapToObj(i -> new ProductEntity(i, "name " + i, i))
//                .collect(Collectors.toList());
//        repository.saveAll(newProducts);
//
//        Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
//        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
//        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
//        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
//    }
//
//    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
//        Page<ProductEntity> productPage = repository.findAll(nextPage);
//        assertEquals(expectedProductIds, productPage.getContent().stream().map(p -> p.getProductId()).collect(Collectors.toList()).toString());
//        assertEquals(expectsNextPage, productPage.hasNext());
//        return productPage.nextPageable();
//    }


    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getName(),           actualEntity.getName());
        assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight());
    }

    private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity) {
        return
                (expectedEntity.getId().equals(actualEntity.getId())) &&
                        (expectedEntity.getVersion() == actualEntity.getVersion()) &&
                        (expectedEntity.getProductId() == actualEntity.getProductId()) &&
                        (expectedEntity.getName().equals(actualEntity.getName())) &&
                        (expectedEntity.getWeight() == actualEntity.getWeight());
    }
}
