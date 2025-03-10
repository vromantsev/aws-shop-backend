package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import ua.reed.config.Configuration;
import ua.reed.dto.ProductDto;
import ua.reed.service.ProductService;
import ua.reed.service.Services;
import ua.reed.utils.LambdaPayloadUtils;

import java.util.Optional;

public class PutProductWithStockLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Configuration LAMBDA_CONFIGURATION = new PutProductWithStockLambdaConfiguration();

    protected ProductService productService = Services.createProductService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(event.toString(), LogLevel.INFO);
        try {
            Optional<ProductDto> productDto = LambdaPayloadUtils.tryParseBody(event.getBody());
            if (productDto.isEmpty() || !LambdaPayloadUtils.isBodyValid(productDto.get())) {
                return LambdaPayloadUtils.createResponse(400, "Invalid payload format - %s".formatted(event.getBody()));
            }
            Optional<ProductDto> newProduct = productService.save(productDto.get());
            return newProduct.map(dto -> LambdaPayloadUtils.createResponse(201, dto))
                    .orElseGet(() -> LambdaPayloadUtils.createErrorResponse("Failed to create a product with stock %s".formatted(productDto.get())));
        } catch (Exception ex) {
            logger.log(ex.getMessage(), LogLevel.ERROR);
            return LambdaPayloadUtils.createDefaultErrorResponse();
        }
    }

    public static Configuration getLambdaConfiguration() {
        return LAMBDA_CONFIGURATION;
    }

    private static class PutProductWithStockLambdaConfiguration implements Configuration {

        @Override
        public String getLambdaJarFilePath() {
            return "../put-product-with-stock-lambda/target/put-product-with-stock-lambda-1.0.0.jar";
        }

        @Override
        public String getHandlerString() {
            return "ua.reed.lambda.PutProductWithStockLambda::handleRequest";
        }

        @Override
        public String getLambdaName() {
            return "PutProductWithStockLambda";
        }
    }
}