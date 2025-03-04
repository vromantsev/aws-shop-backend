package ua.reed.lambda.config;

import ua.reed.lambda.PutProductWithStockLambda;
import ua.reed.service.ProductService;

/**
 * This is a test wrapper allowing to test lambda functionality, and avoid getting aws credentials errors.
 */
public class PutProductWithStockTestableLambda extends PutProductWithStockLambda {

    public PutProductWithStockTestableLambda(final ProductService productService) {
        this.productService = productService;
    }
}
