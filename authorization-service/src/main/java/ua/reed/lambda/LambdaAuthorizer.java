package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.exception.ExceptionUtils;
import ua.reed.config.LambdaConfiguration;
import ua.reed.utils.Constants;
import ua.reed.utils.LambdaPayloadUtils;

import java.util.Base64;
import java.util.Map;

public class LambdaAuthorizer implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final LambdaConfiguration LAMBDA_CONFIG = new LambdaAuthorizerConfig();

    private final Dotenv dotenv = Dotenv.load();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent event, final Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            logger.log("Received parameters: %s".formatted(event.toString()));
            Map<String, String> headers = event.getHeaders();

            String authorization = headers.get("Authorization");
            if (authorization == null || authorization.isBlank() || !authorization.startsWith("Basic")) {
                return LambdaPayloadUtils.createResponse(401, "Unauthorized: auth token is missing or invalid!");
            }
            Base64.Decoder decoder = Base64.getDecoder();
            String credentials = new String(decoder.decode(authorization.substring(6)));
            String[] authParts = credentials.split(":");
            if (authParts.length != 2) {
                return LambdaPayloadUtils.createResponse(401, "Unauthorized: invalid token format!");
            }
            String username = dotenv.get(Constants.USERNAME_KEY);
            String password = dotenv.get(Constants.PASS_KEY);
            if (!authParts[0].equals(username) || !authParts[1].equals(password)) {
                return LambdaPayloadUtils.createResponse(403, "Forbidden: invalid credentials!");
            }
            return LambdaPayloadUtils.createResponse(200, generatePolicy(authParts[0], "Allow", event.getResource()));
        } catch (Exception ex) {
            logger.log(ExceptionUtils.getStackTrace(ex), LogLevel.ERROR);
            return LambdaPayloadUtils.createDefaultErrorResponse();
        }
    }

    public static LambdaConfiguration getLambdaConfiguration() {
        return LAMBDA_CONFIG;
    }

    private Map<String, Object> generatePolicy(final String principalId, final String effect, final String resource) {
        return Map.of(
                "principalId", principalId,
                "policyDocument", Map.of(
                        "Version", "2012-10-17",
                        "Statement", new Object[]{
                                Map.of(
                                        "Action", "execute-api:Invoke",
                                        "Effect", effect,
                                        "Resource", resource
                                )
                        }
                )
        );
    }

    private static class LambdaAuthorizerConfig implements LambdaConfiguration {

        @Override
        public String getLambdaJarFilePath() {
            return "../authorization-service/target/authorization-service-1.0.0.jar";
        }

        @Override
        public String getHandlerString() {
            return "ua.reed.lambda.LambdaAuthorizer::handleRequest";
        }

        @Override
        public String getLambdaName() {
            return "LambdaAuthorizer";
        }
    }
}
