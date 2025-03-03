package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.reed.dto.ProductDto;
import ua.reed.entity.ProductWithStock;
import ua.reed.lambda.config.GetProductListTestLambda;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.ProductMapper;
import ua.reed.mock.MockContext;
import ua.reed.mock.MockData;
import ua.reed.service.ProductService;
import ua.reed.utils.LambdaPayloadUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetProductListLambdaTest {

    @Mock
    private ProductService productServiceMock;

    private GetProductListTestLambda getProductListLambda;

    private AutoCloseable autoCloseable;
    private final Mapper<ProductWithStock, ProductDto> productMapper = new ProductMapper();
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        getProductListLambda = new GetProductListTestLambda(productServiceMock);
    }

    @AfterEach
    public void destroy() throws Exception {
        autoCloseable.close();
    }

    @Test
    void whenGetProductListThenReturnProducts() throws JsonProcessingException {
        // given
        List<ProductWithStock> products = MockData.getProducts();
        List<ProductDto> productDtos = products.stream().map(productMapper::fromSource).collect(Collectors.toList());

        // when
        when(productServiceMock.getProducts()).thenAnswer(invocationOnMock -> productDtos);

        // then
        APIGatewayProxyResponseEvent response = getProductListLambda.handleRequest(
                new APIGatewayProxyRequestEvent(), new MockContext()
        );
        List<ProductDto> responseBody = mapper.readValue(response.getBody(), new TypeReference<>() {
        });

        // assertions & verification
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(responseBody.size(), productDtos.size());
        assertEquals(response, LambdaPayloadUtils.createResponse(200, productDtos));
        verify(productServiceMock, atMostOnce()).getProducts();
    }
}
