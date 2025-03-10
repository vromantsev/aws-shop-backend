package ua.reed.infrastructure;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.Cors;
import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.CorsRule;
import software.amazon.awscdk.services.s3.HttpMethods;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;
import ua.reed.config.Configuration;
import ua.reed.entity.Product;
import ua.reed.entity.Stock;
import ua.reed.lambda.GetProductByIdLambda;
import ua.reed.lambda.GetProductListLambda;
import ua.reed.lambda.ImportProductFileLambda;
import ua.reed.lambda.PutProductWithStockLambda;
import ua.reed.utils.Constants;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static ua.reed.utils.Constants.PRODUCTS_TABLE_EXISTS_ID;
import static ua.reed.utils.Constants.PRODUCTS_TABLE_ID;
import static ua.reed.utils.Constants.PRODUCTS_TABLE_NAME;
import static ua.reed.utils.Constants.PRODUCT_TABLE_ENV_KEY;
import static ua.reed.utils.Constants.STOCKS_TABLE_EXISTS_ID;
import static ua.reed.utils.Constants.STOCKS_TABLE_ID;
import static ua.reed.utils.Constants.STOCKS_TABLE_NAME;
import static ua.reed.utils.Constants.STOCK_TABLE_ENV_KEY;

public class AwsShopBackendStack extends Stack {

    public AwsShopBackendStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // S3 imports bucket
        IBucket importsBucket = createBucketIfNotExists();

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
                                PRODUCT_TABLE_ENV_KEY, productsTable.getTableName(),
                                STOCK_TABLE_ENV_KEY, stocksTable.getTableName()
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
                                PRODUCT_TABLE_ENV_KEY, productsTable.getTableName(),
                                STOCK_TABLE_ENV_KEY, stocksTable.getTableName()
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
                                PRODUCT_TABLE_ENV_KEY, productsTable.getTableName(),
                                STOCK_TABLE_ENV_KEY, stocksTable.getTableName()
                        )
                )
                .build();

        // putProductWithStockLambda can access both tables
        productsTable.grantReadWriteData(putProductWithStockLambda);
        stocksTable.grantReadWriteData(putProductWithStockLambda);

        // importProductFileLambda
        Configuration importProductFileLambdaConfiguration = ImportProductFileLambda.getLambdaConfiguration();
        Function importProductFileLambda = Function.Builder.create(this, importProductFileLambdaConfiguration.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get(importProductFileLambdaConfiguration.getLambdaJarFilePath()).toFile().getPath()))
                .handler(importProductFileLambdaConfiguration.getHandlerString())
                .memorySize(512)
                .environment(Map.of(
                        Constants.IMPORT_BUCKET_NAME_KEY, importsBucket.getBucketName()
                ))
                .build();

        Role lambdaRole = Role.Builder.create(this, "RsAppLambdaExecutionRole")
                .assumedBy(ServicePrincipal.Builder.create("lambda.amazonaws.com").build())
                .managedPolicies(
                        List.of(
                                ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"),
                                ManagedPolicy.fromAwsManagedPolicyName("AmazonS3FullAccess")
                        )
                )
                .build();

        // permissions for S3 bucket
        importsBucket.grantReadWrite(lambdaRole);

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

        LambdaIntegration importProductFileLambdaIntegration = LambdaIntegration.Builder.create(importProductFileLambda)
                .proxy(true)
                .build();

        // /products
        IResource restApiRoot = restApi.getRoot();
        Resource importProductFile = restApiRoot.addResource(Constants.IMPORT_FILE_PATH);
        importProductFile.addMethod(
                "GET",
                importProductFileLambdaIntegration,
                MethodOptions.builder()
                        .requestParameters(Map.of("method.request.querystring.name", true))
                        .build()
        );

        Resource products = restApiRoot.addResource(PRODUCTS_TABLE_NAME);
        products.addMethod("GET", productsLambdaIntegration);
        products.addMethod("POST", putProductLambdaIntegration);

        // /products/{productId}
        Resource productId = products.addResource("{productId}");

        LambdaIntegration productByIdLambda = LambdaIntegration.Builder.create(getProductByIdLambda)
                .proxy(true)
                .build();
        productId.addMethod("GET", productByIdLambda);
    }

    private IBucket createBucketIfNotExists() {
        try {
            return Bucket.fromBucketName(this, Constants.EXISTING_IMPORT_BUCKET_ID, Constants.IMPORT_BUCKET_NAME);
        } catch (Exception ex) {
            return Bucket.Builder.create(this, Constants.NEW_IMPORT_BUCKET_ID)
                    .bucketName(Constants.IMPORT_BUCKET_NAME)
                    .versioned(true)
                    .encryption(BucketEncryption.S3_MANAGED)
                    .cors(List.of(
                            CorsRule.builder()
                                    .allowedHeaders(List.of("*"))
                                    .allowedMethods(
                                            List.of(
                                                    HttpMethods.POST,
                                                    HttpMethods.GET,
                                                    HttpMethods.PUT,
                                                    HttpMethods.DELETE,
                                                    HttpMethods.HEAD
                                            )
                                    )
                                    .allowedOrigins(List.of("*"))
                                    .maxAge(Duration.parse("5").toMinutes())
                                    .build()
                    ))
                    .blockPublicAccess(
                            BlockPublicAccess.Builder.create()
                                    .blockPublicPolicy(false)
                                    .build()
                    )
                    .build();
        }
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
