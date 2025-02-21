package ua.reed.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.nio.file.Paths;

public class GetProductListStack extends Stack {

    public GetProductListStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public GetProductListStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // GetProductListLambda
        Function getProductListLambda = Function.Builder.create(this, "GetProductListLambda")
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get("../get-product-list-lambda/target/get-product-list-lambda-1.0.0.jar").toFile().getPath()))
                .handler("ua.reed.lambda.GetProductListLambda::handleRequest")
                .memorySize(512)
                .build();

        // GetProductByIdLambda
        Function getProductByIdLambda = Function.Builder.create(this, "GetProductByIdLambda")
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get("../get-product-by-id-lambda/target/get-product-by-id-lambda-1.0.0.jar").toFile().getPath()))
                .handler("ua.reed.lambda.GetProductByIdLambda::handleRequest")
                .memorySize(512)
                .build();

        // API Gateway
        RestApi restApi = RestApi.Builder.create(this, "ProductsRestApi")
                .restApiName("ProductsApi")
                .description("REST api that provides integration with multiple Lambda functions")
                .build();

        LambdaIntegration productsLambdaIntegration = LambdaIntegration.Builder.create(getProductListLambda)
                .proxy(true)
                .build();

        // /products
        Resource products = restApi.getRoot().addResource("products");
        products.addMethod("GET", productsLambdaIntegration);

        // /products/{productId}
        Resource productId = products.addResource("{productId}");

        LambdaIntegration productByIdLambda = LambdaIntegration.Builder.create(getProductByIdLambda)
                .proxy(true)
                .build();
        productId.addMethod("GET", productByIdLambda);
    }
}
