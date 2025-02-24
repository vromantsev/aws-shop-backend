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

    private static final String PRODUCT_ID_KEY = "productId";

    private final ProductService productService = Services.create();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        try {
            Map<String, String> pathParameters = event.getPathParameters();
            String productIdAsString = pathParameters.get(PRODUCT_ID_KEY);
            Optional<ProductDto> productOptional = productService.getProductById(UUID.fromString(productIdAsString));
            return productOptional
                    .map(productDto -> LambdaPayloadUtils.createResponse(200, productDto))
                    .orElseGet(() -> LambdaPayloadUtils.createResponse(404, Map.of("message", "Product with id=%s not found!".formatted(productIdAsString))));
        } catch (Exception ex) {
            return LambdaPayloadUtils.createDefaultErrorResponse();
        }
    }
}