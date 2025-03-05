package ua.reed.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDto(String description,
                         UUID id,
                         BigDecimal price,
                         String title,
                         int count) {
}
