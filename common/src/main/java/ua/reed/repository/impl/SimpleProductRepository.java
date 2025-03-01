package ua.reed.repository.impl;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import ua.reed.entity.Product;
import ua.reed.entity.ProductWithStock;
import ua.reed.entity.Stock;
import ua.reed.repository.ProductRepository;
import ua.reed.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleProductRepository implements ProductRepository {

    private final DynamoDbClient dynamoDbClient;

    public SimpleProductRepository(final DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public List<ProductWithStock> getProducts() {
        ScanRequest scanProductsRequest = ScanRequest.builder()
                .tableName(Constants.PRODUCTS_TABLE_NAME)
                .build();
        ScanResponse scanProductsResponse = this.dynamoDbClient.scan(scanProductsRequest);

        ScanRequest scanStocksRequest = ScanRequest.builder()
                .tableName(Constants.STOCKS_TABLE_NAME)
                .build();
        ScanResponse scanStocksResponse = this.dynamoDbClient.scan(scanStocksRequest);

        return scanProductsResponse.items().stream()
                .map(ProductWithStock::fromMap)
                .filter(Objects::nonNull)
                .map(p -> {
                            scanStocksResponse.items()
                                    .stream()
                                    .filter(attributes -> p.getId().toString().equals(attributes.get(Product.ID_FIELD).s()))
                                    .findAny()
                                    .ifPresent(attributes -> p.setCount(Integer.parseInt(attributes.get(Stock.COUNT_FIELD).n())));
                            return p;
                        }
                )
                .toList();
    }

    @Override
    public Optional<ProductWithStock> getProductById(final UUID productId) {
        @SuppressWarnings("unchecked") KeysAndAttributes productKeys = KeysAndAttributes.builder()
                .keys(Map.of(Product.ID_FIELD, AttributeValue.builder().s(productId.toString()).build()))
                .build();
        @SuppressWarnings("unchecked") KeysAndAttributes stockKeys = KeysAndAttributes.builder()
                .keys(Map.of(Stock.ID_FIELD, AttributeValue.builder().s(productId.toString()).build()))
                .build();
        BatchGetItemRequest request = BatchGetItemRequest.builder()
                .requestItems(
                        Map.of(
                                Constants.PRODUCTS_TABLE_NAME, productKeys,
                                Constants.STOCKS_TABLE_NAME, stockKeys
                        )
                )
                .build();
        BatchGetItemResponse batchGetItemResponse = dynamoDbClient.batchGetItem(request);
        List<Map<String, AttributeValue>> products = batchGetItemResponse.responses().get(Constants.PRODUCTS_TABLE_NAME);
        List<Map<String, AttributeValue>> stocks = batchGetItemResponse.responses().get(Constants.STOCKS_TABLE_NAME);
        return ProductWithStock.fromMapWithWrapper(combineAttributes(products, stocks));
    }

    @Override
    public ProductWithStock save(final Product product) {
        return null;
    }

    private Map<String, AttributeValue> combineAttributes(final List<Map<String, AttributeValue>> products,
                                                          final List<Map<String, AttributeValue>> stocks) {
        return Stream.concat(products.stream(), stocks.stream())
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
