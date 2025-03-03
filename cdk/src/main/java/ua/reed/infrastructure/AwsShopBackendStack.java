package ua.reed.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.Cors;
import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import ua.reed.config.Configuration;
import ua.reed.entity.Product;
import ua.reed.entity.Stock;
import ua.reed.lambda.GetProductByIdLambda;
import ua.reed.lambda.GetProductListLambda;
import ua.reed.lambda.PutProductWithStockLambda;

import java.nio.file.Paths;
import java.util.Map;

import static ua.reed.utils.Constants.PRODUCTS_TABLE_EXISTS_ID;
import static ua.reed.utils.Constants.PRODUCTS_TABLE_ID;
import static ua.reed.utils.Constants.PRODUCTS_TABLE_NAME;
import static ua.reed.utils.Constants.STOCKS_TABLE_EXISTS_ID;
import static ua.reed.utils.Constants.STOCKS_TABLE_ID;
import static ua.reed.utils.Constants.STOCKS_TABLE_NAME;

public class AwsShopBackendStack extends Stack {

    public AwsShopBackendStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // DynamoDB products table
        ITable productsTable = createTableIfNotExists(PRODUCTS_TABLE_EXISTS_ID, PRODUCTS_TABLE_ID, PRODUCTS_TABLE_NAME, Product.ID_FIELD);

        // DynamoDB stocks table
        ITable stocksTable = createTableIfNotExists(STOCKS_TABLE_EXISTS_ID, STOCKS_TABLE_ID, STOCKS_TABLE_NAME, Stock.ID_FIELD);

        // GetProductListLambda
        Configuration getProductListLambdaConfiguration = GetProductListLambda.getLambdaConfiguration();
        Function getProductListLambda = Function.Builder.create(this, getProductListLambdaConfiguration.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get(getProductListLambdaConfiguration.getLambdaJarFilePath()).toFile().getPath()))
                .handler(getProductListLambdaConfiguration.getHandlerString())
                .memorySize(512)
                .environment(
                        Map.of(
                                "PRODUCT_TABLE", productsTable.getTableName(),
                                "STOCK_TABLE", stocksTable.getTableName()
                        )
                )
                .build();

        // getProductListLambda can access both tables
        productsTable.grantReadWriteData(getProductListLambda);
        stocksTable.grantReadWriteData(getProductListLambda);

        // GetProductByIdLambda
        Configuration getProductByIdLambdaConfiguration = GetProductByIdLambda.getLambdaConfiguration();
        Function getProductByIdLambda = Function.Builder.create(this, getProductByIdLambdaConfiguration.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get(getProductByIdLambdaConfiguration.getLambdaJarFilePath()).toFile().getPath()))
                .handler(getProductByIdLambdaConfiguration.getHandlerString())
                .memorySize(512)
                .environment(
                        Map.of(
                                "PRODUCT_TABLE", productsTable.getTableName(),
                                "STOCK_TABLE", stocksTable.getTableName()
                        )
                )
                .build();

        // getProductByIdLambda can access both tables
        productsTable.grantReadWriteData(getProductByIdLambda);
        stocksTable.grantReadWriteData(getProductByIdLambda);

        // PutProductWithStockLambda
        Configuration putProductWithStockLambdaConfiguration = PutProductWithStockLambda.getLambdaConfiguration();
        Function putProductWithStockLambda = Function.Builder.create(this, putProductWithStockLambdaConfiguration.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get(putProductWithStockLambdaConfiguration.getLambdaJarFilePath()).toFile().getPath()))
                .handler(putProductWithStockLambdaConfiguration.getHandlerString())
                .memorySize(512)
                .environment(
                        Map.of(
                                "PRODUCT_TABLE", productsTable.getTableName(),
                                "STOCK_TABLE", stocksTable.getTableName()
                        )
                )
                .build();

        // putProductWithStockLambda can access both tables
        productsTable.grantReadWriteData(putProductWithStockLambda);
        stocksTable.grantReadWriteData(putProductWithStockLambda);

        // API Gateway
        RestApi restApi = RestApi.Builder.create(this, "ProductsRestApi")
                .restApiName("ProductsApi")
                .description("REST api that provides integration with multiple Lambda functions")
                .defaultCorsPreflightOptions(
                        CorsOptions.builder()
                                .allowOrigins(Cors.ALL_ORIGINS)
                                .allowMethods(Cors.ALL_METHODS)
                                .allowHeaders(Cors.DEFAULT_HEADERS)
                                .build()
                )
                .build();

        LambdaIntegration productsLambdaIntegration = LambdaIntegration.Builder.create(getProductListLambda)
                .proxy(true)
                .build();

        LambdaIntegration putProductLambdaIntegration = LambdaIntegration.Builder.create(putProductWithStockLambda)
                .proxy(true)
                .build();

        // /products
        Resource products = restApi.getRoot().addResource("products");
        products.addMethod("GET", productsLambdaIntegration);
        products.addMethod("POST", putProductLambdaIntegration);

        // /products/{productId}
        Resource productId = products.addResource("{productId}");

        LambdaIntegration productByIdLambda = LambdaIntegration.Builder.create(getProductByIdLambda)
                .proxy(true)
                .build();
        productId.addMethod("GET", productByIdLambda);
    }

    private ITable createTableIfNotExists(final String tableExistsId,
                                          final String tableId,
                                          final String tableName,
                                          final String primaryKey) {
        try {
            return Table.fromTableName(this, tableExistsId, tableName);
        } catch (Exception ex) {
            return Table.Builder.create(this, tableId)
                    .tableName(tableName)
                    .partitionKey(Attribute.builder()
                            .name(primaryKey)
                            .type(AttributeType.STRING)
                            .build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .removalPolicy(RemovalPolicy.RETAIN)
                    .deletionProtection(true)
                    .build();
        }
    }
}
