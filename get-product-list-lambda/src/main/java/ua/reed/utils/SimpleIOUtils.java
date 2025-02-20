package ua.reed.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.reed.entity.Product;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public final class SimpleIOUtils {

    private SimpleIOUtils() {}

    public static List<Product> readProductsFromJson(final String path) {
        try (InputStream is = SimpleIOUtils.class.getClassLoader().getResourceAsStream(Objects.requireNonNull(path))) {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(is, new TypeReference<List<Product>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Cannot read mock data", e);
        }
    }
}
