package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.reed.lambda.config.ImportProductFileTestableLambda;
import ua.reed.mock.MockContext;
import ua.reed.service.S3ObjectService;
import ua.reed.utils.Constants;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImportProductFileLambdaTest {

    private static final String PRODUCTS_CSV = "products.csv";
    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_PRESIGNED_URL = "https://%s.s3.eu-north-1.amazonaws.com/uploaded/%s".formatted(TEST_BUCKET, PRODUCTS_CSV);

    @Mock
    private S3ObjectService s3ObjectServiceMock;

    private ImportProductFileTestableLambda lambda;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void setup() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        lambda = new ImportProductFileTestableLambda(s3ObjectServiceMock);
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void whenPutCsvFileThenGeneratePreSignedUrlAndReturnSuccessfulResponse() {
        // given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setQueryStringParameters(Map.of(Constants.CSV_FILENAME_KEY, PRODUCTS_CSV));

        // when
        when(s3ObjectServiceMock.generatePreSignedUrlForObject(eq(PRODUCTS_CSV)))
                .thenReturn(Optional.of(TEST_PRESIGNED_URL));

        // then
        APIGatewayProxyResponseEvent response = lambda.handleRequest(event, new MockContext());

        // assertions & verification
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(TEST_PRESIGNED_URL, response.getBody());
        verify(s3ObjectServiceMock, times(1)).generatePreSignedUrlForObject(PRODUCTS_CSV);
    }

    @Test
    public void whenPutCsvFileAndPreSignerErrorOccurredThenReturnErrorResponse() {
        // given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setQueryStringParameters(Map.of(Constants.CSV_FILENAME_KEY, PRODUCTS_CSV));

        // when
        when(s3ObjectServiceMock.generatePreSignedUrlForObject(eq(PRODUCTS_CSV)))
                .thenReturn(Optional.empty());

        // then
        APIGatewayProxyResponseEvent response = lambda.handleRequest(event, new MockContext());

        // assertions & verification
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
        assertEquals("Failed to generate pre-signed URL, file '%s' does not exist.".formatted(PRODUCTS_CSV), response.getBody());
        verify(s3ObjectServiceMock, times(1)).generatePreSignedUrlForObject(PRODUCTS_CSV);
    }

    @Test
    public void whenPutCsvFileAndNameParameterIsMissingThenReturnErrorResponse() {
        // given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();

        // then
        APIGatewayProxyResponseEvent response = lambda.handleRequest(event, new MockContext());

        // assertions & verification
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertEquals("Request parameter [%s] is mandatory!".formatted(Constants.CSV_FILENAME_KEY), response.getBody());
        verify(s3ObjectServiceMock, never()).generatePreSignedUrlForObject(PRODUCTS_CSV);
    }

    @Test
    public void whenPutCsvFileAndUnexpectedErrorHappenedThenReturnErrorResponse() {
        // given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setQueryStringParameters(Map.of(Constants.CSV_FILENAME_KEY, PRODUCTS_CSV));

        // when
        when(s3ObjectServiceMock.generatePreSignedUrlForObject(eq(PRODUCTS_CSV)))
                .thenThrow(new RuntimeException());

        // then
        APIGatewayProxyResponseEvent response = lambda.handleRequest(event, new MockContext());

        // assertions & verification
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
        assertEquals("{\"message\":\"Internal Server Error\"}", response.getBody());
        verify(s3ObjectServiceMock, times(1)).generatePreSignedUrlForObject(PRODUCTS_CSV);
    }
}
