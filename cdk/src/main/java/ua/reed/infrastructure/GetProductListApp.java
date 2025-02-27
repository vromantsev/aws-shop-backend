package ua.reed.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class GetProductListApp {

    private static final String AWS_REGION = "eu-north-1";
    private static final String AWS_ACCOUNT_ID = "your-account-id";

    public static void main(final String[] args) {
        App app = new App();
        new GetProductListStack(app, "GetProductListJavaStack", StackProps.builder()
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

