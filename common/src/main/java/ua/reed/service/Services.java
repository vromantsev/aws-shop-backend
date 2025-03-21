package ua.reed.service;

import ua.reed.config.DynamoDbConfig;
import ua.reed.config.S3Config;
import ua.reed.config.S3PreSignerConfiguration;
import ua.reed.config.SnsConfig;
import ua.reed.config.SqsConfig;
import ua.reed.dto.ProductDto;
import ua.reed.entity.ProductWithStock;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.ProductMapper;
import ua.reed.persistence.Persister;
import ua.reed.repository.ProductRepository;
import ua.reed.repository.impl.SimpleProductRepository;
import ua.reed.service.impl.SimpleCsvProductParserService;
import ua.reed.service.impl.SimplePreSignedUrlService;
import ua.reed.service.impl.SimpleProductService;
import ua.reed.service.impl.SimpleS3ObjectService;
import ua.reed.service.impl.SimpleSnsService;
import ua.reed.service.impl.SimpleSqsService;

public final class Services {

    private Services() {}

    public static ProductService createProductService() {
        Persister persister = new Persister(DynamoDbConfig.getDynamoDbClient());
        ProductRepository productRepository = new SimpleProductRepository(persister);
        Mapper<ProductWithStock, ProductDto> productMapper = new ProductMapper();
        return new SimpleProductService(productRepository, productMapper);
    }

    public static S3ObjectService createS3ObjectService() {
        return new SimpleS3ObjectService(
                new SimplePreSignedUrlService(S3PreSignerConfiguration.getS3Presigner()),
                S3Config.getS3Client()
        );
    }

    public static CsvProductParserService createCsvProductParserService() {
        return new SimpleCsvProductParserService();
    }

    public static SqsService createSqsService() {
        return new SimpleSqsService(SqsConfig.getSqsClient());
    }

    public static SnsService createSnsService() {
        return new SimpleSnsService(SnsConfig.getSnsClient());
    }
}
