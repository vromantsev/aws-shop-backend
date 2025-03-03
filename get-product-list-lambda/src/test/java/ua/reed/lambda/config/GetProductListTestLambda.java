package ua.reed.lambda.config;

import ua.reed.lambda.GetProductListLambda;
import ua.reed.service.ProductService;

public class GetProductListTestLambda extends GetProductListLambda {

    public GetProductListTestLambda(final ProductService productService) {
        this.productService = productService;
    }
}
