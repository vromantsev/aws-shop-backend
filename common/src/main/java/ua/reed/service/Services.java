package ua.reed.service;

import ua.reed.config.DynamoDbConfig;
import ua.reed.dto.ProductDto;
import ua.reed.entity.Product;
import ua.reed.entity.ProductWithStock;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.ProductMapper;
import ua.reed.repository.ProductRepository;
import ua.reed.repository.impl.SimpleProductRepository;

public final class Services {

    private Services() {}

    public static ProductService create() {
        ProductRepository productRepository = new SimpleProductRepository(DynamoDbConfig.getDynamoDbClient());
        Mapper<ProductWithStock, ProductDto> productMapper = new ProductMapper();
        return new SimpleProductService(productRepository, productMapper);
    }
}
