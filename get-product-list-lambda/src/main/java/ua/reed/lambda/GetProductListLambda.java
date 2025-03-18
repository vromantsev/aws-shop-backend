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

import java.util.List;

public class GetProductListLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final LambdaConfiguration LAMBDA_CONFIGURATION = new GetProductListLambdaConfig();

    protected ProductService productService = Services.createProductService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(event.toString(), LogLevel.INFO);
        try {
            List<ProductDto> products = productService.getProducts();
            return LambdaPayloadUtils.createResponse(200, products);
        } catch (Exception ex) {
            logger.log(ExceptionUtils.getStackTrace(ex));
            return LambdaPayloadUtils.createDefaultErrorResponse();
        }
    }

    public static LambdaConfiguration getLambdaConfiguration() {
        return LAMBDA_CONFIGURATION;
    }

    private static class GetProductListLambdaConfig implements LambdaConfiguration {

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
