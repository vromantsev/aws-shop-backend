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
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.SnapStartConf;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.CorsRule;
import software.amazon.awscdk.services.s3.EventType;
import software.amazon.awscdk.services.s3.HttpMethods;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.s3.NotificationKeyFilter;
import software.amazon.awscdk.services.s3.notifications.LambdaDestination;
import software.amazon.awscdk.services.sns.Subscription;
import software.amazon.awscdk.services.sns.SubscriptionProtocol;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;
import ua.reed.config.LambdaConfiguration;
import ua.reed.entity.Product;
import ua.reed.entity.Stock;
import ua.reed.lambda.CatalogBatchProcessLambda;
import ua.reed.lambda.GetProductByIdLambda;
import ua.reed.lambda.GetProductListLambda;
import ua.reed.lambda.ImportFileParserLambda;
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
        LambdaConfiguration getProductListLambdaConfiguration = GetProductListLambda.getLambdaConfiguration();
        Function getProductListLambda = Function.Builder.create(this, getProductListLambdaConfiguration.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get(getProductListLambdaConfiguration.getLambdaJarFilePath()).toFile().getPath()))
                .handler(getProductListLambdaConfiguration.getHandlerString())
                .memorySize(512)
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
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
        LambdaConfiguration getProductByIdLambdaConfiguration = GetProductByIdLambda.getLambdaConfiguration();
        Function getProductByIdLambda = Function.Builder.create(this, getProductByIdLambdaConfiguration.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get(getProductByIdLambdaConfiguration.getLambdaJarFilePath()).toFile().getPath()))
                .handler(getProductByIdLambdaConfiguration.getHandlerString())
                .memorySize(512)
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
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
        LambdaConfiguration putProductWithStockLambdaConfiguration = PutProductWithStockLambda.getLambdaConfiguration();
        Function putProductWithStockLambda = Function.Builder.create(this, putProductWithStockLambdaConfiguration.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get(putProductWithStockLambdaConfiguration.getLambdaJarFilePath()).toFile().getPath()))
                .handler(putProductWithStockLambdaConfiguration.getHandlerString())
                .memorySize(512)
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
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
        LambdaConfiguration importProductFileLambdaConfiguration = ImportProductFileLambda.getLambdaConfiguration();
        Function importProductFileLambda = Function.Builder.create(this, importProductFileLambdaConfiguration.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.seconds(30))
                .code(Code.fromAsset(Paths.get(importProductFileLambdaConfiguration.getLambdaJarFilePath()).toFile().getPath()))
                .handler(importProductFileLambdaConfiguration.getHandlerString())
                .memorySize(512)
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
                .environment(
                        Map.of(
                                Constants.IMPORT_BUCKET_NAME_KEY, importsBucket.getBucketName()
                        )
                )
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


        // catalogItemsQueue
        Queue catalogItemsQueue = Queue.Builder.create(this, Constants.CATALOG_ITEMS_QUEUE_ID)
                .queueName(Constants.CATALOG_ITEMS_QUEUE_NAME)
                .visibilityTimeout(Duration.minutes(1))
                .receiveMessageWaitTime(Duration.seconds(20))
                .retentionPeriod(Duration.hours(1))
                .build();

        // importFileParserLambda
        LambdaConfiguration importFileParserLambdaConfig = ImportFileParserLambda.getLambdaConfiguration();
        Function importFileParserLambda = Function.Builder.create(this, importFileParserLambdaConfig.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.minutes(1))
                .code(Code.fromAsset(Paths.get(importFileParserLambdaConfig.getLambdaJarFilePath()).toFile().getPath()))
                .handler(importFileParserLambdaConfig.getHandlerString())
                .memorySize(512)
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
                .environment(
                        Map.of(
                                Constants.IMPORT_BUCKET_NAME_KEY, importsBucket.getBucketName(),
                                Constants.CATALOG_ITEMS_QUEUE_KEY, catalogItemsQueue.getQueueUrl()
                        )
                )
                .build();

        // trigger importFileParserLambda only if a new object was stored in uploaded/ folder
        importsBucket.addEventNotification(
                EventType.OBJECT_CREATED,
                new LambdaDestination(importFileParserLambda),
                NotificationKeyFilter.builder()
                        .prefix(Constants.UPLOAD_S3_DIRECTORY)
                        .build()
        );

        // catalogBatchProcessLambda
        LambdaConfiguration catalogBatchProcessLambdaConfig = CatalogBatchProcessLambda.getLambdaConfiguration();
        Function catalogBatchProcessLambda = Function.Builder.create(this, catalogBatchProcessLambdaConfig.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .timeout(Duration.minutes(1))
                .code(Code.fromAsset(Paths.get(catalogBatchProcessLambdaConfig.getLambdaJarFilePath()).toFile().getPath()))
                .handler(catalogBatchProcessLambdaConfig.getHandlerString())
                .memorySize(512)
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
                .environment(Map.of(Constants.CATALOG_ITEMS_QUEUE_KEY, catalogItemsQueue.getQueueUrl()))
                .build();

        // catalogBatchProcessLambda is granted read/write permissions to interact with dynamoDb
        productsTable.grantReadWriteData(catalogBatchProcessLambda);
        stocksTable.grantReadWriteData(catalogBatchProcessLambda);

        // sending 5 messages at a time to catalogBatchProcessLambda
        SqsEventSource batchOfFiveEventSource = SqsEventSource.Builder.create(catalogItemsQueue)
                .batchSize(5)
                .reportBatchItemFailures(true)
                .build();

        // assigning event source to catalogBatchProcessLambda
        catalogBatchProcessLambda.addEventSource(batchOfFiveEventSource);

        // importFileParserLambda can send messages to SQS
        PolicyStatement lambdasCanReadWriteToSqsQueue = PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(
                        List.of(
                                "sqs:ChangeMessageVisibility",
                                "sqs:DeleteMessage",
                                "sqs:GetQueueAttributes",
                                "sqs:GetQueueUrl",
                                "sqs:SendMessage",
                                "sqs:ReceiveMessage"
                        )
                )
                .resources(List.of(catalogItemsQueue.getQueueArn()))
                .build();

        importFileParserLambda.addToRolePolicy(lambdasCanReadWriteToSqsQueue);
        catalogBatchProcessLambda.addToRolePolicy(lambdasCanReadWriteToSqsQueue);

        createSnsTopicAndSubscription(catalogBatchProcessLambda);

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

    private void createSnsTopicAndSubscription(final Function lambdaFunction) {
            // define a topic
            Topic topic = Topic.Builder.create(this, Constants.SNS_EMAIL_TOPIC_ID)
                    .topicName(Constants.SNS_EMAIL_TOPIC_NAME)
                    .build();

            // define email subscription
            Subscription.Builder.create(this, Constants.SNS_EMAIL_SIBSCRIPTION_ID)
                    .topic(topic)
                    .protocol(SubscriptionProtocol.EMAIL)
                    .endpoint("vladyslav.romantsev@gmail.com")
                    .build();

            // SNS permissions
            PolicyStatement publishPermissions = PolicyStatement.Builder.create()
                    .effect(Effect.ALLOW)
                    .actions(List.of("sns:Publish", "sns:ListTopics"))
                    .resources(List.of("*"))
                    .build();

            // assign permissions to catalogBatchProcessLambda
            lambdaFunction.addToRolePolicy(publishPermissions);

            // catalogBatchProcessLambda is allowed to publish to this topic
            topic.grantPublish(lambdaFunction);
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
