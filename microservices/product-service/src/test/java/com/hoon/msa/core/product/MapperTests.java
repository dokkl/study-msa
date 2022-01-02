package com.hoon.msa.core.product;

import com.hoon.api.core.product.Product;
import com.hoon.msa.core.product.persistence.ProductEntity;
import com.hoon.msa.core.product.services.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class MapperTests {

    private ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    public void mapperTests() {

        assertNotNull(mapper);

        Product api = new Product(1, "n", 1, "sa");

        ProductEntity entity = mapper.apiToEntity(api);
        log.info("entity.getProductId() : {}", entity.getProductId());
        log.info("entity.getName() : {}", entity.getName());
        log.info("entity.getWeight() : {}", entity.getWeight());

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getWeight(), entity.getWeight());

        Product api2 = mapper.entityToApi(entity);
        log.info("api2.getProductId() : {}", api2.getProductId());
        log.info("api2.getName() : {}", api2.getName());
        log.info("api2.getWeight() : {}", api2.getWeight());
        log.info("api2.getServiceAddress() : {}", api2.getServiceAddress());

        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getName(),      api2.getName());
        assertEquals(api.getWeight(),    api2.getWeight());
        assertNull(api2.getServiceAddress());
    }
}
