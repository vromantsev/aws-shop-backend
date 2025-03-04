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
import ua.reed.lambda.config.PutProductWithStockTestableLambda;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.ProductMapper;
import ua.reed.mock.MockContext;
import ua.reed.mock.MockData;
import ua.reed.service.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PutProductWithStockLambdaTest {

    @Mock
    private ProductService productServiceMock;

    private PutProductWithStockTestableLambda lambda;

    private AutoCloseable autoCloseable;
    private Mapper<ProductWithStock, ProductDto> productMapper = new ProductMapper();
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        lambda = new PutProductWithStockTestableLambda(productServiceMock);
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void whenPutNewProductWithStockThenOperationSuccessful() throws JsonProcessingException {
        // given
        List<ProductWithStock> products = MockData.getProducts();
        ProductWithStock productWithStock = products.get(0);
        ProductDto productDto = productMapper.fromSource(productWithStock);
        var request = new APIGatewayProxyRequestEvent().withBody(mapper.writeValueAsString(productDto));


        // when
        when(productServiceMock.save(eq(productDto))).thenReturn(Optional.of(productDto));

        // then
        APIGatewayProxyResponseEvent response = lambda.handleRequest(request, new MockContext());

        // assertions & verification
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        assertEquals(productDto, mapper.readValue(response.getBody(), ProductDto.class));
        verify(productServiceMock, times(1)).save(productDto);
    }

    @Test
    public void whenPutNewProductWithNullTitleThenReturnError() throws JsonProcessingException {
        // given
        List<ProductWithStock> products = MockData.getProducts();
        ProductWithStock productWithStock = products.get(1);
        productWithStock.setTitle(null);
        ProductDto productDto = productMapper.fromSource(productWithStock);
        var request = new APIGatewayProxyRequestEvent().withBody(mapper.writeValueAsString(productDto));

        // then
        APIGatewayProxyResponseEvent response = lambda.handleRequest(request, new MockContext());

        // assertions & verification
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid payload format - %s".formatted(request.getBody())));
        verify(productServiceMock, never()).save(productDto);
    }

    @Test
    public void whenPutNewProductWithEmptyTitleThenReturnError() throws JsonProcessingException {
        // given
        List<ProductWithStock> products = MockData.getProducts();
        ProductWithStock productWithStock = products.get(1);
        productWithStock.setTitle("");
        ProductDto productDto = productMapper.fromSource(productWithStock);
        var request = new APIGatewayProxyRequestEvent().withBody(mapper.writeValueAsString(productDto));

        // then
        APIGatewayProxyResponseEvent response = lambda.handleRequest(request, new MockContext());

        // assertions & verification
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid payload format - %s".formatted(request.getBody())));
        verify(productServiceMock, never()).save(productDto);
    }

    @Test
    public void whenSaveProductThenUnexpectedErrorOccurred() throws JsonProcessingException {
        // given
        List<ProductWithStock> products = MockData.getProducts();
        ProductWithStock productWithStock = products.get(0);
        ProductDto productDto = productMapper.fromSource(productWithStock);
        var request = new APIGatewayProxyRequestEvent().withBody(mapper.writeValueAsString(productDto));

        // when
        when(productServiceMock.save(eq(productDto))).thenReturn(Optional.empty());

        // then
        APIGatewayProxyResponseEvent response = lambda.handleRequest(request, new MockContext());

        // assertions & verification
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to create a product with stock %s".formatted(productDto)));
        verify(productServiceMock, times(1)).save(productDto);
    }
}
