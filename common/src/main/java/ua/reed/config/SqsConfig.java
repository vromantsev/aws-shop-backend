package ua.reed.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

public final class SqsConfig {

    private static final SqsClient SQS_CLIENT = SqsClient.builder()
            .region(Region.EU_NORTH_1)
            .build();

    private SqsConfig() {}

    public static SqsClient getSqsClient() {
        return SQS_CLIENT;
    }
}
