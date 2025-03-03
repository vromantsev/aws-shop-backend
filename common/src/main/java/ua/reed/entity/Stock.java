package ua.reed.entity;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Stock {

    public static final String ID_FIELD = "product_id";
    public static final String COUNT_FIELD = "count";

    private UUID productId;
    private int count;

    public Stock() {
    }

    public Stock(UUID productId, int count) {
        this.productId = productId;
        this.count = count;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return count == stock.count && Objects.equals(productId, stock.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, count);
    }

    @Override
    public String toString() {
        return "Stock{" +
                "productId=" + productId +
                ", count=" + count +
                '}';
    }

    public static Map<String, AttributeValue> toItem(final ProductWithStock productWithStock) {
        return Map.of(
                ID_FIELD, AttributeValue.builder().s(productWithStock.getId().toString()).build(),
                COUNT_FIELD, AttributeValue.builder().n(String.valueOf(productWithStock.getCount())).build()
        );
    }
}
