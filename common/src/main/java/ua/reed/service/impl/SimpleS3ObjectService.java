package ua.reed.service.impl;

import ua.reed.service.PreSignedUrlService;
import ua.reed.service.S3ObjectService;
import ua.reed.utils.Constants;

import java.util.Optional;

public class SimpleS3ObjectService implements S3ObjectService {

    private final PreSignedUrlService preSignedUrlService;

    public SimpleS3ObjectService(final PreSignedUrlService preSignedUrlService) {
        this.preSignedUrlService = preSignedUrlService;
    }

    @Override
    public Optional<String> generatePreSignedUrlForObject(final String fileName) {
        String fileNameWithPrefix = Constants.UPLOAD_S3_DIRECTORY + fileName;
        return this.preSignedUrlService.generatePreSignedUrl(System.getenv(Constants.IMPORT_BUCKET_NAME_KEY), fileNameWithPrefix);
    }
}
