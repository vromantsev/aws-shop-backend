package ua.reed.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public final class DynamoDbConfig {

    private DynamoDbConfig() {}

    private static final DynamoDbClient DYNAMO_DB_CLIENT = DynamoDbClient.builder()
            .region(Region.EU_NORTH_1)
            .build();

    public static DynamoDbClient getDynamoDbClient() {
        return DYNAMO_DB_CLIENT;
    }
}
