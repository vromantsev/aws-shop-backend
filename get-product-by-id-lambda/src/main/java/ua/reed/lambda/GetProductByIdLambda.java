package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import ua.reed.config.LambdaConfiguration;
import ua.reed.dto.ProductDto;
import ua.reed.service.ProductService;
import ua.reed.service.Services;
import ua.reed.utils.LambdaPayloadUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GetProductByIdLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final LambdaConfiguration LAMBDA_CONFIGURATION = new GetProductByIdLambdaConfig();

    private static final String PRODUCT_ID_KEY = "productId";

    protected ProductService productService = Services.createProductService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(event.toString(), LogLevel.INFO);
        try {
            Map<String, String> pathParameters = event.getPathParameters();
            String productIdAsString = pathParameters.get(PRODUCT_ID_KEY);
            if (LambdaPayloadUtils.isProductIdValid(productIdAsString)) {
                Optional<ProductDto> productOptional = productService.getProductById(UUID.fromString(productIdAsString));
                return productOptional
                        .map(productDto -> LambdaPayloadUtils.createResponse(200, productDto))
                        .orElseGet(() -> LambdaPayloadUtils.createResponse(404, Map.of("message", "Product with id=%s not found!".formatted(productIdAsString))));
            }
            return LambdaPayloadUtils.createErrorResponse("Product id is of invalid type, got %s, expected UUID".formatted(productIdAsString));
        } catch (Exception ex) {
            logger.log(ExceptionUtils.getStackTrace(ex));
            return LambdaPayloadUtils.createDefaultErrorResponse();
        }
    }

    public static LambdaConfiguration getLambdaConfiguration() {
        return LAMBDA_CONFIGURATION;
    }

    private static class GetProductByIdLambdaConfig implements LambdaConfiguration {

        @Override
        public String getLambdaJarFilePath() {
            return "../get-product-by-id-lambda/target/get-product-by-id-lambda-1.0.0.jar";
        }

        @Override
        public String getHandlerString() {
            return "ua.reed.lambda.GetProductByIdLambda::handleRequest";
        }

        @Override
        public String getLambdaName() {
            return "GetProductByIdLambda";
        }
    }
}