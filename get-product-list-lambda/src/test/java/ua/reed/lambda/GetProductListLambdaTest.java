package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.reed.dto.ProductDto;
import ua.reed.entity.Product;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.ProductMapper;
import ua.reed.mock.MockContext;
import ua.reed.mock.MockData;
import ua.reed.service.ProductService;
import ua.reed.utils.LambdaPayloadUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetProductListLambdaTest {

    private static final String PRODUCTS_KEY = "products";

    @Mock
    private static ProductService productServiceMock;

    @InjectMocks
    private static RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> getProductListLambda;

    private AutoCloseable autoCloseable;
    private final Mapper<Product, ProductDto> productMapper = new ProductMapper();
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void destroy() throws Exception {
        autoCloseable.close();
    }

    @BeforeAll
    public static void setup() {
        getProductListLambda = new GetProductListLambda();
    }

    @Test
    void whenGetProductListThenReturnProducts() throws JsonProcessingException {
        // given
        List<Product> products = MockData.getProducts();
        List<ProductDto> productDtos = products.stream().map(productMapper::fromSource).collect(Collectors.toList());

        // when
        when(productServiceMock.getProducts()).thenReturn(productDtos);

        // then
        APIGatewayProxyResponseEvent response = getProductListLambda.handleRequest(
                new APIGatewayProxyRequestEvent(), new MockContext()
        );
        @SuppressWarnings("unchecked")
        List<ProductDto> responseBody = (List<ProductDto>) mapper.readValue(response.getBody(), Map.class).get(PRODUCTS_KEY);

        // assertions & verification
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(responseBody.size(), productDtos.size());
        assertEquals(response, LambdaPayloadUtils.createResponse(200, LambdaPayloadUtils.defaultCorsHeaders(), Map.of(PRODUCTS_KEY, productDtos)));
        verify(productServiceMock, atMostOnce()).getProducts();
    }
}
