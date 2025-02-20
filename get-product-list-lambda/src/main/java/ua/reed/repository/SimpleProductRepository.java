package ua.reed.repository;

import ua.reed.entity.Product;
import ua.reed.utils.SimpleIOUtils;

import java.util.List;

public class SimpleProductRepository implements ProductRepository {

    private static final String MOCK_DATA_PATH = "mock-data.json";

    @Override
    public List<Product> getProducts() {
        return SimpleIOUtils.readProductsFromJson(MOCK_DATA_PATH);
    }
}
