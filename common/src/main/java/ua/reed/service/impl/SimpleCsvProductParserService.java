package ua.reed.service.impl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ua.reed.entity.Product;
import ua.reed.entity.ProductWithStock;
import ua.reed.entity.Stock;
import ua.reed.service.CsvProductParserService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleCsvProductParserService implements CsvProductParserService {

    private static final Logger LOGGER = Logger.getLogger(SimpleCsvProductParserService.class.getSimpleName());

    @Override
    public List<ProductWithStock> fromCsv(byte[] csvAsBytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(csvAsBytes);
             CSVParser csvParser = CSVParser.parse(bais, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            return csvParser.stream()
                    .map(r -> new ProductWithStock(
                                    null,
                                    getNotEmptyColumnValue(r, Product.DESCRIPTION_FIELD),
                                    BigDecimal.valueOf(Double.parseDouble(getNotEmptyColumnValue(r, Product.PRICE_FIELD))),
                                    getNotEmptyColumnValue(r, Product.TITLE_FIELD),
                                    Integer.parseInt(getNotEmptyColumnValue(r, Stock.COUNT_FIELD))
                            )
                    ).toList();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Failed to parse a csv file");
            return Collections.emptyList();
        }
    }

    private String getNotEmptyColumnValue(final CSVRecord record, final String columnName) {
        String value = record.get(columnName);
        if (value.isEmpty()) {
            value = record.get(columnName.substring(0, 1).toUpperCase() + columnName.substring(1));
        }
        return value;
    }
}
