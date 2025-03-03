package ua.reed.persistence;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsResponse;
import ua.reed.entity.Product;
import ua.reed.entity.ProductWithStock;
import ua.reed.entity.Stock;
import ua.reed.utils.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Persister {

    private static final Logger LOGGER = Logger.getLogger(Persister.class.getSimpleName());

    private final DynamoDbClient dynamoDbClient;

    public Persister(final DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public Optional<ProductWithStock> putInTxReturning(final ProductWithStock productWithStock) {
        try {
            Put putProduct = Put.builder()
                    .tableName(Constants.PRODUCTS_TABLE_NAME)
                    .item(Product.toItem(productWithStock))
                    .build();
            Put putStock = Put.builder()
                    .tableName(Constants.STOCKS_TABLE_NAME)
                    .item(Stock.toItem(productWithStock))
                    .build();
            TransactWriteItemsRequest writeItemsRequest = TransactWriteItemsRequest.builder()
                    .transactItems(
                            List.of(
                                    TransactWriteItem.builder().put(putProduct).build(),
                                    TransactWriteItem.builder().put(putStock).build()
                            )
                    )
                    .build();
            TransactWriteItemsResponse writeItemsResponse = dynamoDbClient.transactWriteItems(writeItemsRequest);
            if (writeItemsResponse.sdkHttpResponse().isSuccessful()) {
                LOGGER.info("Transactional write status: %d".formatted(writeItemsResponse.sdkHttpResponse().statusCode()));
                return Optional.of(productWithStock);
            }
            return Optional.empty();
        } catch (DynamoDbException dynamoDbException) {
            LOGGER.log(Level.SEVERE, dynamoDbException, () -> "Failed to store product '%s' with stock %d".formatted(productWithStock.getDescription(), productWithStock.getCount()));
            return Optional.empty();
        }
    }

    public List<ProductWithStock> getProducts() {
        try {
            ScanRequest scanProductsRequest = ScanRequest.builder()
                    .tableName(Constants.PRODUCTS_TABLE_NAME)
                    .build();
            ScanResponse scanProductsResponse = this.dynamoDbClient.scan(scanProductsRequest);
            List<ProductWithStock> productsWithoutStock = scanProductsResponse.items().stream()
                    .map(ProductWithStock::fromMap)
                    .toList();
            ScanRequest scanStocksRequest = ScanRequest.builder()
                    .tableName(Constants.STOCKS_TABLE_NAME)
                    .build();
            ScanResponse scanStocksResponse = this.dynamoDbClient.scan(scanStocksRequest);
            productsWithoutStock.forEach(productWithStock -> scanStocksResponse.items()
                    .stream()
                    .filter(attributes -> productWithStock.getId().equals(UUID.fromString(attributes.get(Product.ID_FIELD).s())))
                    .findAny()
                    .ifPresent(attributes -> productWithStock.setCount(Integer.parseInt(attributes.get(Stock.COUNT_FIELD).n()))));
            return productsWithoutStock;
        } catch (DynamoDbException dynamoDbException) {
            LOGGER.log(Level.SEVERE, dynamoDbException, () -> "Failed to get all products with stocks");
            return Collections.emptyList();
        }
    }

    public Optional<ProductWithStock> getById(final UUID productId) {
        try {
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
        } catch (DynamoDbException dynamoDbException) {
            LOGGER.log(Level.SEVERE, dynamoDbException, () -> "Cannot get product with stock by id %s".formatted(productId));
            return Optional.empty();
        }
    }

    private Map<String, AttributeValue> combineAttributes(final List<Map<String, AttributeValue>> products,
                                                          final List<Map<String, AttributeValue>> stocks) {
        return Stream.concat(products.stream(), stocks.stream())
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
