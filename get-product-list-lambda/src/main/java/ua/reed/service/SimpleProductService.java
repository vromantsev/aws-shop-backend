package ua.reed.service;

import ua.reed.dto.ProductDto;
import ua.reed.entity.Product;
import ua.reed.mapper.Mapper;
import ua.reed.repository.ProductRepository;

import java.util.List;

public class SimpleProductService implements ProductService {

    private final ProductRepository productRepository;
    private final Mapper<Product, ProductDto> productMapper;

    public SimpleProductService(final ProductRepository productRepository,
                                final Mapper<Product, ProductDto> productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductDto> getProducts() {
        List<Product> products = productRepository.getProducts();
        return products.stream()
                .map(productMapper::fromSource)
                .toList();
    }
}
