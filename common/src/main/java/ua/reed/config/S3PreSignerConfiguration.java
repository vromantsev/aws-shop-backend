package ua.reed.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public final class S3PreSignerConfiguration {

    private static final S3Presigner S3_PRESIGNER = S3Presigner.builder()
            .region(Region.EU_NORTH_1)
            .build();

    private S3PreSignerConfiguration() {}

    public static S3Presigner getS3Presigner() {
        return S3_PRESIGNER;
    }
}
