package ua.reed.service.impl;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import ua.reed.dto.CopyS3ObjectResponse;
import ua.reed.dto.DeleteS3ObjectResponse;
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

    @Override
    public DeleteS3ObjectResponse deleteObject(final String bucket, final String fileNameWithPrefix) {
        try {
            DeleteObjectResponse response = this.s3Client.deleteObject(builder -> builder.bucket(bucket).key(fileNameWithPrefix));
            SdkHttpResponse sdkHttpResponse = response.sdkHttpResponse();
            LOGGER.info("Delete object status: %d, message: '%s'".formatted(sdkHttpResponse.statusCode(), sdkHttpResponse.statusText()));
            return DeleteS3ObjectResponse.of(sdkHttpResponse.statusCode(), sdkHttpResponse.statusText().orElse("Delete object operation is completed."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex, () -> "Cannot delete object from S3. Bucket: '%s', key: '%s'".formatted(bucket, fileNameWithPrefix));
            return DeleteS3ObjectResponse.withError("Failed to delete object from S3. Bucket: '%s', key: '%s'".formatted(bucket, fileNameWithPrefix));
        }
    }

    @Override
    public CopyS3ObjectResponse copyObject(final String bucket, final String sourceFileNameWithPrefix, final String destinationFileNameWithPrefix) {
        try {
            CopyObjectResponse response = this.s3Client.copyObject(builder -> builder
                    .sourceBucket(bucket)
                    .sourceKey(sourceFileNameWithPrefix)
                    .destinationBucket(bucket)
                    .destinationKey(destinationFileNameWithPrefix)
            );
            SdkHttpResponse sdkHttpResponse = response.sdkHttpResponse();
            LOGGER.info("Copy object status: %d, message: '%s'".formatted(sdkHttpResponse.statusCode(), sdkHttpResponse.statusText().orElse("Copy object operation is completed.")));
            if (sdkHttpResponse.isSuccessful()) {
                return CopyS3ObjectResponse.ofSuccess(sdkHttpResponse.statusCode(), sdkHttpResponse.statusText().orElse("Copy object operation is completed."));
            }
            return CopyS3ObjectResponse.of(sdkHttpResponse.statusCode(), sdkHttpResponse.statusText().orElse("Failed to copy an object."), false);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex, () -> "Cannot copy S3 object from '%s' to: '%s'".formatted(bucket + "/" + sourceFileNameWithPrefix, bucket + "/" + destinationFileNameWithPrefix));
            return CopyS3ObjectResponse.ofFailure("Cannot copy S3 object from '%s' to: '%s'".formatted(bucket + "/" + sourceFileNameWithPrefix, bucket + "/" + destinationFileNameWithPrefix));
        }
    }
}
