package ua.reed.lambda.config;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.reed.lambda.ImportFileParserLambda;
import ua.reed.mapper.Mapper;
import ua.reed.service.CsvProductParserService;
import ua.reed.service.S3ObjectService;

import java.util.List;

/**
 * This is a test wrapper allowing to test lambda functionality, and avoid getting aws credentials errors.
 */
public class ImportFileParserTestableLambda extends ImportFileParserLambda {

    public ImportFileParserTestableLambda(final ObjectMapper objectMapper,
                                          final Mapper<JsonNode, List<S3EventNotification.S3EventNotificationRecord>> s3RecordsMapper,
                                          final S3ObjectService s3ObjectService,
                                          final CsvProductParserService csvProductParserService) {
        this.objectMapper = objectMapper;
        this.s3RecordsMapper = s3RecordsMapper;
        this.s3ObjectService = s3ObjectService;
        this.csvProductParserService = csvProductParserService;
    }
}
