package ua.reed.lambda;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.reed.dto.ProductDto;
import ua.reed.entity.ProductWithStock;
import ua.reed.lambda.config.CatalogBatchProcessTestableLambda;
import ua.reed.mapper.Mapper;
import ua.reed.mapper.ProductMapper;
import ua.reed.mock.MockContext;
import ua.reed.mock.MockData;
import ua.reed.service.ProductService;
import ua.reed.service.SnsService;
import ua.reed.utils.JsonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CatalogBatchProcessLambdaTest {

    @Mock
    private ProductService productServiceMock;

    @Mock
    private SnsService snsServiceMock;

    private CatalogBatchProcessTestableLambda lambda;

    private AutoCloseable autoCloseable;
    private final Mapper<ProductWithStock, ProductDto> mapper = new ProductMapper();

    @BeforeEach
    public void setup() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        lambda = new CatalogBatchProcessTestableLambda(productServiceMock, snsServiceMock);
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void whenGetRecordsFromSQSEventThenCreateProductsAndSendEmailSuccessfully() {
        // given
        SQSEvent event = new SQSEvent();

        SQSEvent.SQSMessage firstSqsMessage = new SQSEvent.SQSMessage();
        ProductWithStock productOne = fromMockData(0);
        firstSqsMessage.setBody(JsonUtils.toJson(productOne));

        SQSEvent.SQSMessage secondSqsMessage = new SQSEvent.SQSMessage();
        ProductWithStock productTwo = fromMockData(1);
        secondSqsMessage.setBody(JsonUtils.toJson(productTwo));

        event.setRecords(List.of(firstSqsMessage, secondSqsMessage));

        ProductDto first = mapper.fromSource(productOne);
        ProductDto second = mapper.fromSource(productTwo);

        // when
        when(productServiceMock.save(any(ProductDto.class)))
                .thenReturn(Optional.of(first))
                .thenReturn(Optional.of(second));
        doNothing().when(snsServiceMock).sendEmailNotification();

        // then
        this.lambda.handleRequest(event, new MockContext());

        // assertions & verification
        verify(productServiceMock, times(2)).save(any(ProductDto.class));
        verify(snsServiceMock, times(1)).sendEmailNotification();
    }

    @Test
    public void whenGetNoRecordsFromSQSEventThenSkipSendingEmailNotification() {
        // given
        SQSEvent event = new SQSEvent();
        event.setRecords(Collections.emptyList());

        // then
        lambda.handleRequest(event, new MockContext());

        // assertions & verification
        verify(productServiceMock, never()).save(any(ProductDto.class));
        verify(snsServiceMock, never()).sendEmailNotification();
    }

    @Test
    public void whenProcessSQSEventRecordsThenExceptionIsThrown() {
        // given
        SQSEvent event = new SQSEvent();

        SQSEvent.SQSMessage firstSqsMessage = new SQSEvent.SQSMessage();
        ProductWithStock productOne = fromMockData(0);
        firstSqsMessage.setBody(JsonUtils.toJson(productOne));

        event.setRecords(List.of(firstSqsMessage));

        // when
        when(productServiceMock.save(any(ProductDto.class)))
                .thenThrow(new RuntimeException());

        // then
        lambda.handleRequest(event, new MockContext());

        // assertions & verification
        verify(productServiceMock, times(1)).save(any(ProductDto.class));
    }

    private ProductWithStock fromMockData(int index) {
        return MockData.getProducts().get(index);
    }
}
