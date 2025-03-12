package ua.reed.service.impl;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import ua.reed.service.PreSignedUrlService;
import ua.reed.service.S3ObjectService;
import ua.reed.utils.Constants;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleS3ObjectService implements S3ObjectService {

    private static final Logger LOGGER = Logger.getLogger(SimpleS3ObjectService.class.getSimpleName());

    private final PreSignedUrlService preSignedUrlService;
    private final S3Client s3Client;

    public SimpleS3ObjectService(final PreSignedUrlService preSignedUrlService, final S3Client s3Client) {
        this.preSignedUrlService = preSignedUrlService;
        this.s3Client = s3Client;
    }

    @Override
    public Optional<String> generatePreSignedUrlForObject(final String fileName) {
        String fileNameWithPrefix = Constants.UPLOAD_S3_DIRECTORY + fileName;
        return this.preSignedUrlService.generatePreSignedUrl(System.getenv(Constants.IMPORT_BUCKET_NAME_KEY), fileNameWithPrefix);
    }

    @Override
    public Optional<byte[]> getObject(final String bucket, final String fileNameWithPrefix) {
        try {
            ResponseInputStream<GetObjectResponse> inputStreamResponse = this.s3Client.getObject(builder -> builder.bucket(bucket).key(fileNameWithPrefix));
            return Optional.of(inputStreamResponse.readAllBytes());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex, () -> "Failed to get object from S3. Bucket: '%s', key: '%s'".formatted(bucket, fileNameWithPrefix));
            return Optional.empty();
        }
    }
}
