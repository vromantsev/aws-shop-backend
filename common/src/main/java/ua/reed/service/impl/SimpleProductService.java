package ua.reed.service.impl;

import ua.reed.dto.ProductDto;
import ua.reed.entity.ProductWithStock;
import ua.reed.mapper.Mapper;
import ua.reed.repository.ProductRepository;
import ua.reed.service.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SimpleProductService implements ProductService {

    private final ProductRepository productRepository;
    private final Mapper<ProductWithStock, ProductDto> productMapper;

    public SimpleProductService(final ProductRepository productRepository,
                                final Mapper<ProductWithStock, ProductDto> productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductDto> getProducts() {
        List<ProductWithStock> products = productRepository.getProducts();
        return products.stream()
                .map(productMapper::fromSource)
                .toList();
    }

    @Override
    public Optional<ProductDto> getProductById(final UUID productId) {
        return productRepository.getProductById(productId)
                .map(productMapper::fromSource);
    }

    @Override
    public Optional<ProductDto> save(final ProductDto product) {
        return productRepository.save(productMapper.fromTarget(product))
                .map(productMapper::fromSource);
    }
}
