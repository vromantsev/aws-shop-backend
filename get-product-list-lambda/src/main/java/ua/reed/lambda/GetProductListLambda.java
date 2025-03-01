package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import ua.reed.config.Configuration;
import ua.reed.dto.ProductDto;
import ua.reed.service.ProductService;
import ua.reed.service.Services;
import ua.reed.utils.LambdaPayloadUtils;

import java.util.List;

public class GetProductListLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Configuration LAMBDA_CONFIGURATION = new GetProductListLambdaConfig();

    private final ProductService productService = Services.create();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        try {
            List<ProductDto> products = productService.getProducts();
            return LambdaPayloadUtils.createResponse(200, products);
        } catch (Exception ex) {
            return LambdaPayloadUtils.createDefaultErrorResponse();
        }
    }

    public static Configuration getLambdaConfiguration() {
        return LAMBDA_CONFIGURATION;
    }

    private static class GetProductListLambdaConfig implements Configuration {

        @Override
        public String getLambdaJarFilePath() {
            return "../get-product-list-lambda/target/get-product-list-lambda-1.0.0.jar";
        }

        @Override
        public String getHandlerString() {
            return "ua.reed.lambda.GetProductListLambda::handleRequest";
        }

        @Override
        public String getLambdaName() {
            return "GetProductListLambda";
        }
    }
}
