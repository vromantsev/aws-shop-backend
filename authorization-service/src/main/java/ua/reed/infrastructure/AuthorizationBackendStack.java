package ua.reed.infrastructure;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.SnapStartConf;
import software.constructs.Construct;
import ua.reed.config.LambdaConfiguration;
import ua.reed.lambda.LambdaAuthorizer;
import ua.reed.utils.Constants;

import java.nio.file.Paths;

public class AuthorizationBackendStack extends Stack {

    public AuthorizationBackendStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // create lambdaAuthorizer
        LambdaConfiguration lambdaConfiguration = LambdaAuthorizer.getLambdaConfiguration();
        Function lambdaAuthorizer = Function.Builder.create(this, lambdaConfiguration.getLambdaName())
                .runtime(Runtime.JAVA_21)
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
                .code(Code.fromAsset(Paths.get(lambdaConfiguration.getLambdaJarFilePath()).toFile().getPath()))
                .handler(lambdaConfiguration.getHandlerString())
                .memorySize(512)
                .timeout(Duration.seconds(30))
                .build();

        // exporting lambdaAuthorizer ARN to the main stack
        CfnOutput.Builder.create(this, Constants.AUTHORIZER_EXPORT_ARN_PARAMETER)
                .value(lambdaAuthorizer.getFunctionArn())
                .description("ARN of the lambda authorizer function")
                .exportName(Constants.AUTHORIZER_ARN_KEY)
                .build();
    }
}
