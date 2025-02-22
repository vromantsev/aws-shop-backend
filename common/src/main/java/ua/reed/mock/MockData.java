package ua.reed.mock;

import ua.reed.entity.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MockData {

    private MockData() {}

    private static final List<Product> PRODUCTS = List.of(
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80aa"), "Short Product Description1", BigDecimal.valueOf(24), "ProductOne"),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a1"), "Short Product Description7", BigDecimal.valueOf(15), "ProductTitle"),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a3"), "Short Product Description2", BigDecimal.valueOf(23), "Product"),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73348a80a1"), "Short Product Description4", BigDecimal.valueOf(15), "ProductTest"),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9445-fc73c48a80a2"), "Short Product Descriptio1", BigDecimal.valueOf(23), "Product2"),
            new Product(UUID.fromString("7567ec4b-b10c-45c5-9345-fc73c48a80a1"), "Short Product Description7", BigDecimal.valueOf(15), "ProductName")
    );

    public static List<Product> getProducts() {
        return new ArrayList<>(PRODUCTS);
    }
}
