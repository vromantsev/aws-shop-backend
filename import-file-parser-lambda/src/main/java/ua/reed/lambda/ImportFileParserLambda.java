package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import ua.reed.config.Configuration;
import ua.reed.dto.ImportFileResponse;

public class ImportFileParserLambda implements RequestHandler<S3Event, ImportFileResponse> {

    private static final Configuration LAMBDA_CONFIGURATION = new ImportFileParserLambdaConfig();

    @Override
    public ImportFileResponse handleRequest(final S3Event input, final Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            return null;
        } catch (Exception ex) {
            logger.log(ex.getMessage(), LogLevel.ERROR);
            return ImportFileResponse.withInternalServerError();
        }
    }

    public static Configuration getLambdaConfiguration() {
        return LAMBDA_CONFIGURATION;
    }

    private static class ImportFileParserLambdaConfig implements Configuration {

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