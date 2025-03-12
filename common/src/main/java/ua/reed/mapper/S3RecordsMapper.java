package ua.reed.mapper;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class S3RecordsMapper implements Mapper<JsonNode, List<S3EventNotification.S3EventNotificationRecord>> {

    private static final Mapper<JsonNode, List<S3EventNotification.S3EventNotificationRecord>> S3_RECORDS_MAPPER = new S3RecordsMapper();

    @Override
    public List<S3EventNotification.S3EventNotificationRecord> fromSource(final JsonNode source) {
        List<S3EventNotification.S3EventNotificationRecord> records = new ArrayList<>();
        for (JsonNode json : source.get("Records")) {
            // Extract required fields
            String eventVersion = json.get("eventVersion").asText();
            String eventSource = json.get("eventSource").asText();
            String awsRegion = json.get("awsRegion").asText();
            String eventTime = json.get("eventTime").asText();
            String eventName = json.get("eventName").asText();

            // Extract S3 Details
            JsonNode s3Node = json.get("s3");
            JsonNode bucketNode = s3Node.get("bucket");
            JsonNode objectNode = s3Node.get("object");

            // Create S3 Bucket & Object
            S3EventNotification.S3BucketEntity bucket = new S3EventNotification.S3BucketEntity(
                    bucketNode.get("name").asText(),
                    new S3EventNotification.UserIdentityEntity(bucketNode.get("ownerIdentity").get("principalId").asText()),
                    bucketNode.get("arn").asText()
            );

            S3EventNotification.S3ObjectEntity object = new S3EventNotification.S3ObjectEntity(
                    objectNode.get("key").asText(),
                    objectNode.get("size").asLong(),
                    objectNode.get("eTag").asText(),
                    objectNode.has("versionId") ? objectNode.get("versionId").asText() : null,
                    objectNode.get("sequencer").asText()
            );

            // Create S3 Entity
            S3EventNotification.S3Entity s3 = new S3EventNotification.S3Entity(
                    s3Node.get("configurationId").asText(),
                    bucket,
                    object,
                    s3Node.get("s3SchemaVersion").asText()
            );

            // Create Record
            S3EventNotification.S3EventNotificationRecord s3EventNotificationRecord = new S3EventNotification.S3EventNotificationRecord(
                    awsRegion,
                    eventName,
                    eventSource,
                    eventTime,
                    eventVersion,
                    null,
                    null,
                    s3,
                    null
            );
            records.add(s3EventNotificationRecord);
        }
        return records;
    }

    @Override
    public JsonNode fromTarget(List<S3EventNotification.S3EventNotificationRecord> target) {
        throw new UnsupportedOperationException("This operation is not supported!");
    }

    public static Mapper<JsonNode, List<S3EventNotification.S3EventNotificationRecord>> getInstance() {
        return S3_RECORDS_MAPPER;
    }
}
