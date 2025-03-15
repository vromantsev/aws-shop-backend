package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import ua.reed.config.LambdaConfiguration;
import ua.reed.service.S3ObjectService;
import ua.reed.service.Services;
import ua.reed.utils.Constants;
import ua.reed.utils.LambdaPayloadUtils;

import java.util.Map;
import java.util.Optional;

public class ImportProductFileLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final LambdaConfiguration LAMBDA_CONFIGURATION = new ImportProductFileLambdaConfig();

    protected S3ObjectService s3ObjectService = Services.createS3ObjectService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(event.toString(), LogLevel.INFO);
        try {
            Map<String, String> queryStringParameters = event.getQueryStringParameters();
            if (queryStringParameters != null && !queryStringParameters.isEmpty()) {
                String csvFileName = queryStringParameters.get(Constants.CSV_FILENAME_KEY);
                Optional<String> preSignedUrl = this.s3ObjectService.generatePreSignedUrlForObject(csvFileName);
                return preSignedUrl
                        .map(url -> LambdaPayloadUtils.createResponse(200, url))
                        .orElseGet(() -> LambdaPayloadUtils.createErrorResponse("Failed to generate pre-signed URL, file '%s' does not exist.".formatted(csvFileName)));
            }
            return LambdaPayloadUtils.createResponse(400, "Request parameter [%s] is mandatory!".formatted(Constants.CSV_FILENAME_KEY));
        } catch (Exception ex) {
            logger.log(ex.getMessage(), LogLevel.ERROR);
            return LambdaPayloadUtils.createDefaultErrorResponse();
        }
    }

    public static LambdaConfiguration getLambdaConfiguration() {
        return LAMBDA_CONFIGURATION;
    }

    private static class ImportProductFileLambdaConfig implements LambdaConfiguration {

        @Override
        public String getLambdaJarFilePath() {
            return "../import-product-file-lambda/target/import-product-file-lambda-1.0.0.jar";
        }

        @Override
        public String getHandlerString() {
            return "ua.reed.lambda.ImportProductFileLambda::handleRequest";
        }

        @Override
        public String getLambdaName() {
            return "ImportProductFileLambda";
        }
    }
}