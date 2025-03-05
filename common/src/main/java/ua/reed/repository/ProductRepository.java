package ua.reed.repository;

import ua.reed.entity.ProductWithStock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    List<ProductWithStock> getProducts();

    Optional<ProductWithStock> getProductById(UUID productId);

    Optional<ProductWithStock> save(ProductWithStock product);

}
