package ua.reed.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import ua.reed.utils.Constants;

public class AuthorizationServiceApp {

    private static final String AWS_REGION = "eu-north-1";
    private static final String AWS_ACCOUNT_ID = "";

    public static void main(final String[] args) {
        App app = new App();
        new AuthorizationBackendStack(app, Constants.AUTH_STACK_ID, StackProps.builder()
                .env(
                        Environment.builder()
                                .account(AWS_ACCOUNT_ID)
                                .region(AWS_REGION)
                                .build()
                )
                .build());
        app.synth();
    }
}