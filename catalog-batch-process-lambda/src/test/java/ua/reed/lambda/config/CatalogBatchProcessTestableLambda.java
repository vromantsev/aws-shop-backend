package ua.reed.lambda.config;

import ua.reed.lambda.CatalogBatchProcessLambda;
import ua.reed.service.ProductService;
import ua.reed.service.SnsService;

public class CatalogBatchProcessTestableLambda extends CatalogBatchProcessLambda {

    public CatalogBatchProcessTestableLambda(final ProductService productService, final SnsService snsService) {
        this.productService = productService;
        this.snsService = snsService;
    }
}
