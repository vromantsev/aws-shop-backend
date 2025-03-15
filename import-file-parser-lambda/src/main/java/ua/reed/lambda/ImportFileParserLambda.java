package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.reed.config.LambdaConfiguration;
import ua.reed.config.JacksonConfig;
import ua.reed.dto.CopyS3ObjectResponse;
import ua.reed.entity.ProductWithStock;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.S3RecordsMapper;
import ua.reed.service.CsvProductParserService;
import ua.reed.service.S3ObjectService;
import ua.reed.service.Services;
import ua.reed.utils.Constants;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

public class ImportFileParserLambda implements RequestStreamHandler {

    private static final Logger LOGGER = Logger.getLogger(ImportFileParserLambda.class.getSimpleName());

    private static final LambdaConfiguration LAMBDA_CONFIGURATION = new ImportFileParserLambdaConfig();

    protected ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
    protected Mapper<JsonNode, List<S3EventNotification.S3EventNotificationRecord>> s3RecordsMapper = S3RecordsMapper.getInstance();
    protected S3ObjectService s3ObjectService = Services.createS3ObjectService();
    protected CsvProductParserService csvProductParserService = Services.createCsvProductParserService();

    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            JsonNode jsonNode = objectMapper.readTree(input);
            List<S3EventNotification.S3EventNotificationRecord> records = s3RecordsMapper.fromSource(jsonNode);
            records.forEach(s3Record -> {
                String bucketName = s3Record.getS3().getBucket().getName();
                String key = s3Record.getS3().getObject().getUrlDecodedKey();
                s3ObjectService.getObject(bucketName, key)
                        .ifPresent(csv -> {
                                    List<ProductWithStock> products = csvProductParserService.fromCsv(csv);
                                    products.forEach(p -> LOGGER.info("Parsed csv record: %s".formatted(p)));
                                    CopyS3ObjectResponse copyResult = s3ObjectService.copyObject(bucketName, key, key.replace(Constants.UPLOAD_S3_DIRECTORY, Constants.PARSED_S3_DIRECTORY));
                                    if (copyResult.isSuccessful()) {
                                        s3ObjectService.deleteObject(bucketName, key);
                                    }
                                }
                        );
            });
        } catch (Exception ex) {
            logger.log(ex.getMessage(), LogLevel.ERROR);
        }
    }

    public static LambdaConfiguration getLambdaConfiguration() {
        return LAMBDA_CONFIGURATION;
    }

    private static class ImportFileParserLambdaConfig implements LambdaConfiguration {

        @Override
        public String getLambdaJarFilePath() {
            return "../import-file-parser-lambda/target/import-file-parser-lambda-1.0.0.jar";
        }

        @Override
        public String getHandlerString() {
            return "ua.reed.lambda.ImportFileParserLambda::handleRequest";
        }

        @Override
        public String getLambdaName() {
            return "ImportFileParserLambda";
        }
    }
}