package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.reed.dto.ProductDto;
import ua.reed.entity.ProductWithStock;
import ua.reed.lambda.config.GetProductByIdTestLambda;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.ProductMapper;
import ua.reed.mock.MockContext;
import ua.reed.mock.MockData;
import ua.reed.service.ProductService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetProductByIdLambdaTest {

    private static final String PRODUCT_ID_KEY = "productId";

    @Mock
    private ProductService productServiceMock;

    private GetProductByIdTestLambda getProductByIdLambda;

    private AutoCloseable autoCloseable;
    private final Mapper<ProductWithStock, ProductDto> productMapper = new ProductMapper();
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        getProductByIdLambda = new GetProductByIdTestLambda(productServiceMock);
    }

    @AfterEach
    public void destroy() throws Exception {
        autoCloseable.close();
    }

    @Test
    void whenGetProductByIdThenReturnProduct() throws JsonProcessingException {
        // given
        List<ProductWithStock> products = MockData.getProducts();
        ProductWithStock randomProduct = products.get(ThreadLocalRandom.current().nextInt(products.size()));
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        if (event.getPathParameters() == null) {
            event.setPathParameters(Map.of(PRODUCT_ID_KEY, randomProduct.getId().toString()));
        }

        // when
        when(productServiceMock.getProductById(eq(randomProduct.getId())))
                .thenReturn(Optional.of(productMapper.fromSource(randomProduct)));

        // then
        APIGatewayProxyResponseEvent response = getProductByIdLambda.handleRequest(event, new MockContext());
        ProductDto responseBody = mapper.readValue(response.getBody(), ProductDto.class);

        // assertions & verification
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertAll(() -> {
            assertEquals(responseBody.id(), randomProduct.getId());
            assertEquals(responseBody.description(), randomProduct.getDescription());
            assertEquals(responseBody.price(), randomProduct.getPrice());
            assertEquals(responseBody.title(), randomProduct.getTitle());
        });
        verify(productServiceMock, atMostOnce()).getProductById(randomProduct.getId());
    }

    @Test
    void whenGetProductByInvalidIdThenReturnError() throws JsonProcessingException {
        // given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        UUID randomId = UUID.randomUUID();
        if (event.getPathParameters() == null) {
            event.setPathParameters(Map.of("productId", randomId.toString()));
        }

        // when
        when(productServiceMock.getProductById(eq(randomId))).thenReturn(Optional.empty());

        // then
        APIGatewayProxyResponseEvent response = getProductByIdLambda.handleRequest(event, new MockContext());
        String message = ((String) mapper.readValue(response.getBody(), Map.class).get("message"));

        // assertions & verification
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertEquals("Product with id=%s not found!".formatted(randomId), message);
        verify(productServiceMock, atMostOnce()).getProductById(randomId);
    }

    @Test
    public void whenGetProductByInvalidIdTypeThenReturnErrorResponse() {
        // given
        String invalidProductId = "123";
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        if (event.getPathParameters() == null) {
            event.withPathParameters(Map.of("productId", invalidProductId));
        }

        // then
        APIGatewayProxyResponseEvent response = getProductByIdLambda.handleRequest(event, new MockContext());
        String responseBody = response.getBody();

        // assertions & verification
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
        assertEquals("Product id is of invalid type, got 123, expected UUID", responseBody);
        verify(productServiceMock, never()).getProductById(any());
    }
}
