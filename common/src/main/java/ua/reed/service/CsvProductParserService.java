package ua.reed.service;

import ua.reed.entity.ProductWithStock;

import java.util.List;

public interface CsvProductParserService {

    List<ProductWithStock> fromCsv(final byte[] csvAsBytes);

}
