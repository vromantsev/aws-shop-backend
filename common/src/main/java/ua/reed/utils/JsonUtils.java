package ua.reed.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.reed.config.JacksonConfig;

public final class JsonUtils {

    private static final ObjectMapper MAPPER = JacksonConfig.getObjectMapper();

    private JsonUtils() {}

    public static <T> String toJson(T body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(final String json, final Class<? extends T> targetClass) {
        try {
            return MAPPER.readValue(json, targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
