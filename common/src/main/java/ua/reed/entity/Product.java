package ua.reed.entity;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Product {

    public static final String ID_FIELD = "id";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String PRICE_FIELD = "price";
    public static final String TITLE_FIELD = "title";

    private UUID id;
    private String description;
    private BigDecimal price;
    private String title;

    public Product() {
    }

    public Product(UUID id, String description, BigDecimal price, String title) {
        this.id = id;
        this.description = description;
        this.price = price;
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", title='" + title + '\'' +
                '}';
    }

    public static Map<String, AttributeValue> toItem(final ProductWithStock productWithStock) {
        if (productWithStock.getId() == null) {
            productWithStock.setId(UUID.randomUUID());
        }
        return Map.of(
                ID_FIELD, AttributeValue.builder().s(productWithStock.getId().toString()).build(),
                DESCRIPTION_FIELD, AttributeValue.builder().s(productWithStock.getDescription()).build(),
                PRICE_FIELD, AttributeValue.builder().n(productWithStock.getPrice().toString()).build(),
                TITLE_FIELD, AttributeValue.builder().s(productWithStock.getTitle()).build()
        );
    }
}
