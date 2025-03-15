package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import ua.reed.config.LambdaConfiguration;

public class CatalogBatchProcessLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final LambdaConfiguration LAMBDA_CONFIGURATION = new CatalogBatchProcessLambdaConfig();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        return null;
    }

    public static LambdaConfiguration getLambdaConfiguration() {
        return LAMBDA_CONFIGURATION;
    }

    private static class CatalogBatchProcessLambdaConfig implements LambdaConfiguration {

        @Override
        public String getLambdaJarFilePath() {
            return "../catalog-batch-process-lambda/target/catalog-batch-process-lambda-1.0.0.jar";
        }

        @Override
        public String getHandlerString() {
            return "ua.reed.lambda.CatalogBatchProcessLambda::handleRequest";
        }

        @Override
        public String getLambdaName() {
            return "CatalogBatchProcessLambda";
        }
    }
}