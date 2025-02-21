package ua.reed.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDto(String description, UUID id, BigDecimal price, String title) {

}
