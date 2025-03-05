package ua.reed.lambda.config;

import ua.reed.lambda.GetProductListLambda;
import ua.reed.service.ProductService;

/**
 * This is a test wrapper allowing to test lambda functionality, and avoid getting aws credentials errors.
 */
public class GetProductListTestableLambda extends GetProductListLambda {

    public GetProductListTestableLambda(final ProductService productService) {
        this.productService = productService;
    }
}
