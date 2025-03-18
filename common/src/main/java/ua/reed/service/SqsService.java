package ua.reed.service;

import ua.reed.entity.ProductWithStock;

import java.util.List;

public interface SqsService {

    void sendWithBatches(List<ProductWithStock> products);

}
