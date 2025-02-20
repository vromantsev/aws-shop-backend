package ua.reed;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.reed.service.ProductService;

public class GetProductListLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        try {
            ProductService productService = Initializer.create();
            responseEvent.setBody(MAPPER.writeValueAsString(productService.getProducts()));
            responseEvent.setStatusCode(200);
            return responseEvent;
        } catch (Exception ex) {
            responseEvent.setStatusCode(500);
            responseEvent.setBody(ex.getMessage());
            return responseEvent;
        }
    }
}
