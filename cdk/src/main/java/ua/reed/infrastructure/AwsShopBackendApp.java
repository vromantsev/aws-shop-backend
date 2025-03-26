package ua.reed.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import ua.reed.utils.Constants;

import static ua.reed.utils.Constants.STACK_ID;

public class AwsShopBackendApp {

    private static final String AWS_REGION = "eu-north-1";
    private static final String AWS_ACCOUNT_ID = "";

    public static void main(final String[] args) {
        App app = new App();
        AuthorizationBackendStack authorizationBackendStack = new AuthorizationBackendStack(app, Constants.AUTH_DEPLOY_STACK_ID, StackProps.builder()
                .env(
                        Environment.builder()
                                .account(AWS_ACCOUNT_ID)
                                .region(AWS_REGION)
                                .build()
                )
                .build());
        AwsShopBackendStack awsShopBackendStack = new AwsShopBackendStack(app, STACK_ID, StackProps.builder()
                .env(
                        Environment.builder()
                                .account(AWS_ACCOUNT_ID)
                                .region(AWS_REGION)
                                .build()
                )
                .build());
        awsShopBackendStack.addDependency(authorizationBackendStack);
        app.synth();
    }
}

