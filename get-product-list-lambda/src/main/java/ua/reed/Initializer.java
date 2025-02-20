package ua.reed;

import ua.reed.dto.ProductDto;
import ua.reed.entity.Product;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.ProductMapper;
import ua.reed.repository.ProductRepository;
import ua.reed.repository.SimpleProductRepository;
import ua.reed.service.ProductService;
import ua.reed.service.SimpleProductService;

public class Initializer {

    public static ProductService create() {
        ProductRepository productRepository = new SimpleProductRepository();
        Mapper<Product, ProductDto> productMapper = new ProductMapper();
        return new SimpleProductService(productRepository, productMapper);
    }
}
