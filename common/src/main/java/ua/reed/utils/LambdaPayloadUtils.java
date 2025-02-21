package ua.reed.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public final class LambdaPayloadUtils {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final Map<String, Object> ERROR_MESSAGE_PAYLOAD = Map.of("message", "Internal Server Error");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LambdaPayloadUtils() {}

    public static APIGatewayProxyResponseEvent createResponse(final int statusCode,
                                                              final Map<String, String> headers,
                                                              final Map<String, Object> body) {
        var response = new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(createBody(body));
        if (headers != null && !headers.isEmpty()) {
            response.withHeaders(headers);
        }
        return response;
    }

    public static APIGatewayProxyResponseEvent createDefaultErrorResponse() {
        return LambdaPayloadUtils.createResponse(500, Map.of(CONTENT_TYPE_HEADER, APPLICATION_JSON), ERROR_MESSAGE_PAYLOAD);
    }

    public static String createBody(Map<String, Object> body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
