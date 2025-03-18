package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import ua.reed.config.LambdaConfiguration;
import ua.reed.dto.ProductDto;
import ua.reed.service.ProductService;
import ua.reed.service.Services;
import ua.reed.utils.JsonUtils;

import java.util.logging.Logger;

public class CatalogBatchProcessLambda implements RequestHandler<SQSEvent, Void> {

    private static final Logger LOGGER = Logger.getLogger(CatalogBatchProcessLambda.class.getSimpleName());

    private static final LambdaConfiguration LAMBDA_CONFIGURATION = new CatalogBatchProcessLambdaConfig();

    protected ProductService productService = Services.createProductService();

    @Override
    public Void handleRequest(final SQSEvent event, final Context context) {
        try {
            event.getRecords().forEach(m -> {
                String body = m.getBody();
                LOGGER.info("Received payload: '%s'".formatted(body));
                productService.save(JsonUtils.fromJson(body, ProductDto.class));
            });
        } catch (Exception ex) {
            LOGGER.severe(ex.getMessage());
        }
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