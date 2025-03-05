package ua.reed.mock;

import ua.reed.entity.Product;
import ua.reed.entity.ProductWithStock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MockData {

    private MockData() {}

    private static final List<ProductWithStock> PRODUCTS = List.of(
            new ProductWithStock(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80aa"), "Short Product Description1", BigDecimal.valueOf(24), "ProductOne", 1),
            new ProductWithStock(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a1"), "Short Product Description7", BigDecimal.valueOf(15), "ProductTitle", 2),
            new ProductWithStock(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a3"), "Short Product Description2", BigDecimal.valueOf(23), "Product", 3),
            new ProductWithStock(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73348a80a1"), "Short Product Description4", BigDecimal.valueOf(15), "ProductTest", 4),
            new ProductWithStock(UUID.fromString("7567ec4b-b10c-48c5-9445-fc73c48a80a2"), "Short Product Descriptio1", BigDecimal.valueOf(23), "Product2", 5),
            new ProductWithStock(UUID.fromString("7567ec4b-b10c-45c5-9345-fc73c48a80a1"), "Short Product Description7", BigDecimal.valueOf(15), "ProductName", 0)
    );

    public static List<ProductWithStock> getProducts() {
        return new ArrayList<>(PRODUCTS);
    }
}
