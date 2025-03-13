package ua.reed.lambda.config;

import ua.reed.lambda.ImportProductFileLambda;
import ua.reed.service.S3ObjectService;

public class ImportProductFileTestableLambda extends ImportProductFileLambda {

    public ImportProductFileTestableLambda(final S3ObjectService s3ObjectService) {
        this.s3ObjectService = s3ObjectService;
    }
}
