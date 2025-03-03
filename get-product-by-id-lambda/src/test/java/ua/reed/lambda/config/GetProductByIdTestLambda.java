package ua.reed.lambda.config;

import ua.reed.lambda.GetProductByIdLambda;
import ua.reed.service.ProductService;

/**
 * This is a test wrapper allowing to test lambda functionality, and avoid getting aws credentials errors.
 */
public class GetProductByIdTestLambda extends GetProductByIdLambda {

    public GetProductByIdTestLambda(final ProductService productService) {
        this.productService = productService;
    }
}
