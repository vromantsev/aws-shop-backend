package ua.reed.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

public final class SnsConfig {

    private static final SnsClient SNS_CLIENT = SnsClient.builder()
            .region(Region.EU_NORTH_1)
            .build();

    private SnsConfig() {}

    public static SnsClient getSnsClient() {
        return SNS_CLIENT;
    }
}
