package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import ua.reed.dto.ProductDto;
import ua.reed.service.ProductService;
import ua.reed.service.Services;
import ua.reed.utils.LambdaPayloadUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GetProductByIdLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String PRODUCT_KEY = "product";

    private final ProductService productService = Services.create();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        try {
            Map<String, String> pathParameters = event.getPathParameters();
            String productIdAsString = pathParameters.get("productId");
            Optional<ProductDto> productOptional = productService.getProductById(UUID.fromString(productIdAsString));
            if (productOptional.isEmpty()) {
                return LambdaPayloadUtils.createResponse(404, null, Map.of("message", "Product with id=%s not found!".formatted(productIdAsString)));
            }
            return LambdaPayloadUtils.createResponse(200, null, Map.of(PRODUCT_KEY, productOptional.get()));
        } catch (Exception ex) {
            return LambdaPayloadUtils.createDefaultErrorResponse();
        }
    }
}