package ua.reed.service;

import ua.reed.dto.ProductDto;
import ua.reed.entity.Product;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.ProductMapper;
import ua.reed.repository.ProductRepository;
import ua.reed.repository.SimpleProductRepository;

public class Services {

    public static ProductService create() {
        ProductRepository productRepository = new SimpleProductRepository();
        Mapper<Product, ProductDto> productMapper = new ProductMapper();
        return new SimpleProductService(productRepository, productMapper);
    }
}
