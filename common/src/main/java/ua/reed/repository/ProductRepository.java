package ua.reed.repository;

import ua.reed.dto.ProductDto;
import ua.reed.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    List<Product> getProducts();

    Optional<Product> getProductById(UUID productId);

}
