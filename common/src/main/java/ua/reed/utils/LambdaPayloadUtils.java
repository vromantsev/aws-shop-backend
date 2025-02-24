package ua.reed.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public final class LambdaPayloadUtils {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final Map<String, Object> ERROR_MESSAGE_PAYLOAD = Map.of("message", "Internal Server Error");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LambdaPayloadUtils() {
    }

    public static <T> APIGatewayProxyResponseEvent createResponse(final int statusCode,
                                                                  final T body) {
        return createResponseInternal(statusCode, null, body);
    }

    private static <T> APIGatewayProxyResponseEvent createResponseInternal(final int statusCode,
                                                                           final Map<String, String> headers,
                                                                           final T body) {
        var response = new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(createBody(body));
        Map<String, String> corsHeaders = defaultCorsHeaders();
        if (headers != null && !headers.isEmpty()) {
            corsHeaders.putAll(headers);
        }
        response.withHeaders(corsHeaders);
        return response;
    }

    public static APIGatewayProxyResponseEvent createDefaultErrorResponse() {
        return LambdaPayloadUtils.createResponseInternal(500, Map.of(CONTENT_TYPE_HEADER, APPLICATION_JSON), ERROR_MESSAGE_PAYLOAD);
    }

    public static <T> String createBody(T body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> defaultCorsHeaders() {
        return new HashMap<>(
                Map.of(
                        "Access-Control-Allow-Origin", "*",
                        "Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS",
                        "Access-Control-Allow-Headers", "Content-Type,Authorization"
                )
        );
    }
}
