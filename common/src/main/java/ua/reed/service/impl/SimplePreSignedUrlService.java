package ua.reed.service.impl;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import ua.reed.service.PreSignedUrlService;

import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ua.reed.utils.Constants.DEFAULT_PRE_SIGNED_URL_DURATION_MINUTES;

public class SimplePreSignedUrlService implements PreSignedUrlService {

    private static final Logger LOGGER = Logger.getLogger(SimplePreSignedUrlService.class.getSimpleName());

    private final S3Presigner s3Presigner;

    public SimplePreSignedUrlService(final S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    @Override
    public Optional<String> generatePreSignedUrl(final String bucketName, final String fileNameWithPrefix) {
        try {
            PresignedPutObjectRequest presignedGetObjectRequest = s3Presigner.presignPutObject(
                    PutObjectPresignRequest.builder()
                            .putObjectRequest(builder -> builder.bucket(bucketName).key(fileNameWithPrefix))
                            .signatureDuration(Duration.ofMinutes(DEFAULT_PRE_SIGNED_URL_DURATION_MINUTES))
                            .build()
            );
            LOGGER.info("Generating pre-signed URL for file: '%s', bucket: '%s'".formatted(fileNameWithPrefix, bucketName));
            return Optional.of(presignedGetObjectRequest.url().toString());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex, () -> "Failed to generate pre-signed URL for file: '%s', bucket: '%s'".formatted(fileNameWithPrefix, bucketName));
            return Optional.empty();
        }
    }
}
