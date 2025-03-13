package ua.reed.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public final class S3Config {

    private static final S3Client S3_CLIENT = S3Client.builder()
            .region(Region.EU_NORTH_1)
            .build();

    private S3Config() {}

    public static S3Client getS3Client() {
        return S3_CLIENT;
    }
}
