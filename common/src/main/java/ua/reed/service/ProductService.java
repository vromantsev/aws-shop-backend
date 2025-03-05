package ua.reed.service;

import ua.reed.dto.ProductDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductService {

    List<ProductDto> getProducts();

    Optional<ProductDto> getProductById(UUID productId);

    Optional<ProductDto> save(ProductDto product);

}
