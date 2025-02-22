package ua.reed.repository;

import ua.reed.entity.Product;
import ua.reed.mock.MockData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SimpleProductRepository implements ProductRepository {

    @Override
    public List<Product> getProducts() {
        return MockData.getProducts();
    }

    @Override
    public Optional<Product> getProductById(final UUID productId) {
        return MockData.getProducts().stream()
                .filter(product -> product.getId().equals(productId))
                .findAny();
    }
}
