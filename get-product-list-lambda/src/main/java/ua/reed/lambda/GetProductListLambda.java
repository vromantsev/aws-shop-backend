package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import ua.reed.dto.ProductDto;
import ua.reed.service.ProductService;
import ua.reed.service.Services;
import ua.reed.utils.LambdaPayloadUtils;

import java.util.List;
import java.util.Map;

public class GetProductListLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String PRODUCTS_KEY = "products";

    private final ProductService productService = Services.create();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        try {
            List<ProductDto> products = productService.getProducts();
            return LambdaPayloadUtils.createResponse(200, LambdaPayloadUtils.defaultCorsHeaders(), Map.of(PRODUCTS_KEY, products));
        } catch (Exception ex) {
            return LambdaPayloadUtils.createDefaultErrorResponse();
        }
    }
}
