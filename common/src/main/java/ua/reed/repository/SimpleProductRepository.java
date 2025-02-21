package ua.reed.repository;

import ua.reed.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SimpleProductRepository implements ProductRepository {

    public static final List<Product> PRODUCTS = List.of(
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80aa"), "Short Product Description1", BigDecimal.valueOf(24), "ProductOne"),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a1"), "Short Product Description7", BigDecimal.valueOf(15), "ProductTitle"),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a3"), "Short Product Description2", BigDecimal.valueOf(23), "Product"),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73348a80a1"), "Short Product Description4", BigDecimal.valueOf(15), "ProductTest"),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9445-fc73c48a80a2"), "Short Product Descriptio1", BigDecimal.valueOf(23), "Product2"),
            new Product(UUID.fromString("7567ec4b-b10c-45c5-9345-fc73c48a80a1"), "Short Product Description7", BigDecimal.valueOf(15), "ProductName")
    );

    @Override
    public List<Product> getProducts() {
        return PRODUCTS;
    }

    @Override
    public Optional<Product> getProductById(final UUID productId) {
        return PRODUCTS.stream()
                .filter(product -> product.getId().equals(productId))
                .findAny();
    }
}
