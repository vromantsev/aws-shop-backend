package ua.reed.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import ua.reed.dto.ProductDto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class LambdaPayloadUtils {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final Map<String, String> DEFAULT_ERROR_HEADERS = Map.of(CONTENT_TYPE_HEADER, APPLICATION_JSON);
    private static final Map<String, Object> ERROR_MESSAGE_PAYLOAD = Map.of("message", "Internal Server Error");

    private LambdaPayloadUtils() {}

    public static <T> APIGatewayProxyResponseEvent createResponse(final int statusCode, final T body) {
        return createResponseInternal(statusCode, null, body);
    }

    private static <T> APIGatewayProxyResponseEvent createResponseInternal(final int statusCode,
                                                                           final Map<String, String> headers,
                                                                           final T body) {
        var response = new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(body instanceof String bodyString ? bodyString : JsonUtils.toJson(body));
        Map<String, String> corsHeaders = defaultCorsHeaders();
        if (headers != null && !headers.isEmpty()) {
            corsHeaders.putAll(headers);
        }
        response.withHeaders(corsHeaders);
        return response;
    }

    public static APIGatewayProxyResponseEvent createDefaultErrorResponse() {
        return LambdaPayloadUtils.createResponseInternal(500, DEFAULT_ERROR_HEADERS, ERROR_MESSAGE_PAYLOAD);
    }

    public static APIGatewayProxyResponseEvent createErrorResponse(final String message) {
        return createResponseInternal(500, DEFAULT_ERROR_HEADERS, message);
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

    public static boolean isProductIdValid(final String productIdAsString) {
        try {
            UUID.fromString(productIdAsString); // check if an exception is thrown
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isCountValid(final String count) {
        try {
            Integer.parseInt(count);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isPriceValid(final String price) {
        try {
            BigDecimal.valueOf(Double.parseDouble(price));
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isBodyValid(final ProductDto productDto) {
        if (productDto.id() != null && !isProductIdValid(productDto.id().toString())) {
            return false;
        }
        String description = productDto.description();
        if (description == null || description.isBlank()) {
            return false;
        }
        BigDecimal price = productDto.price();
        if (price != null && !isPriceValid(String.valueOf(price.doubleValue()))) {
            return false;
        }
        String title = productDto.title();
        if (title == null || title.isBlank()) {
            return false;
        }
        int count = productDto.count();
        if (!isCountValid(String.valueOf(count))) {
            return false;
        }
        return true;
    }

    public static Optional<ProductDto> tryParseBody(final String body) {
        return Optional.ofNullable(JsonUtils.fromJson(body, ProductDto.class));
    }
}
