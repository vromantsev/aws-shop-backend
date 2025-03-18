package ua.reed.service.impl;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;
import ua.reed.entity.ProductWithStock;
import ua.reed.exceptions.FailedToSendSqsMessagesException;
import ua.reed.service.SqsService;
import ua.reed.utils.Constants;
import ua.reed.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class SimpleSqsService implements SqsService {

    private static final Logger LOGGER = Logger.getLogger(SimpleSqsService.class.getSimpleName());

    private final SqsClient sqsClient;

    public SimpleSqsService(final SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public void sendWithBatches(final List<ProductWithStock> products) {
        try {
            Map<Integer, List<SendMessageBatchRequestEntry>> batches = Batcher.transformToBatches(products);
            batches.forEach((batchNumber, batch) -> {
                SendMessageBatchResponse batchResponse = processBatch(batch);
                if (batchResponse.hasFailed()) {
                    batchResponse.failed().forEach(e -> LOGGER.severe("Failed to send a message: '%s'".formatted(e)));
                }
            });
        } catch (Exception ex) {
            throw new FailedToSendSqsMessagesException("Failed to send %d messages".formatted(products.size()), ex);
        }
    }

    private SendMessageBatchResponse processBatch(final List<SendMessageBatchRequestEntry> batch) {
        return this.sqsClient.sendMessageBatch(builder ->
                builder
                        .queueUrl(System.getenv(Constants.CATALOG_ITEMS_QUEUE_KEY))
                        .entries(batch));
    }

    private static class Batcher {

        private static final int BATCH_SIZE = 5;

        private Batcher() {
        }

        static Map<Integer, List<SendMessageBatchRequestEntry>> transformToBatches(final List<ProductWithStock> products) {
            if (products.size() <= BATCH_SIZE) {
                List<SendMessageBatchRequestEntry> batch = createEntriesFrom(products);
                LOGGER.info("Copy batch before clearing: %s".formatted(batch));
                return Map.of(BATCH_SIZE, batch);
            }
            Map<Integer, List<SendMessageBatchRequestEntry>> entries = new HashMap<>();
            List<SendMessageBatchRequestEntry> batch = new LinkedList<>();
            for (int i = 0; i < products.size(); i++) {
                if (batch.size() == BATCH_SIZE) {
                    var copy = batch;
                    entries.put(i, copy);
                    LOGGER.info("Copy batch before clearing: %s".formatted(copy));
                    batch = new ArrayList<>();
                    LOGGER.info("Copy batch after clearing: %s".formatted(copy));
                    continue;
                }
                batch.add(fromProduct(products.get(i)));
            }
            LOGGER.info("Result: %s".formatted(entries));
            return entries;
        }

        private static List<SendMessageBatchRequestEntry> createEntriesFrom(final List<ProductWithStock> products) {
            return products.stream()
                    .map(Batcher::fromProduct)
                    .toList();
        }

        private static SendMessageBatchRequestEntry fromProduct(final ProductWithStock product) {
            return SendMessageBatchRequestEntry.builder()
                    .id(UUID.randomUUID().toString())
                    .messageBody(JsonUtils.toJson(product))
                    .build();
        }
    }
}
