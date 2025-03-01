package ua.reed.entity;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ua.reed.entity.Stock.COUNT_FIELD;

public class ProductWithStock extends Product {

    private int count;

    public ProductWithStock(int count) {
        this.count = count;
    }

    public ProductWithStock(UUID id, String description, BigDecimal price, String title, int count) {
        super(id, description, price, title);
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public static Optional<ProductWithStock> fromMapWithWrapper(final Map<String, AttributeValue> attributes) {
        return Optional.ofNullable(fromMap(attributes));
    }

    public static ProductWithStock fromMap(final Map<String, AttributeValue> attributes) {
        if (attributes.isEmpty()) {
            return null;
        }
        return new ProductWithStock(
                UUID.fromString(attributes.get(Product.ID_FIELD).s()),
                attributes.get(DESCRIPTION_FIELD).s(),
                BigDecimal.valueOf(Long.parseLong(attributes.get(PRICE_FIELD).n())),
                attributes.get(TITLE_FIELD).s(),
                attributes.containsKey(COUNT_FIELD) ? Integer.parseInt(attributes.get(COUNT_FIELD).n()) : 0
        );
    }
}
