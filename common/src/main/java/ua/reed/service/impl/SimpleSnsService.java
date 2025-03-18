package ua.reed.service.impl;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Topic;
import ua.reed.service.SnsService;
import ua.reed.utils.Constants;

import java.util.Objects;
import java.util.Optional;

public class SimpleSnsService implements SnsService {

    private final SnsClient snsClient;

    public SimpleSnsService(final SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    @Override
    public boolean doesTopicExist(final String topicName) {
        Objects.requireNonNull(topicName, "Parameter [topicName] must not be null!");
        return getTopicByName(topicName).isPresent();
    }

    @Override
    public Optional<Topic> getTopicByName(final String topicName) {
        Objects.requireNonNull(topicName, "Parameter [topicName] must not be null!");
        return snsClient.listTopics()
                .topics()
                .stream()
                .filter(t -> t.topicArn().contains(topicName))
                .findAny();
    }

    @Override
    public void sendEmailNotification() {
        Topic topic = getTopicByName(Constants.SNS_EMAIL_TOPIC_NAME)
                .orElseThrow();
        snsClient.publish(builder -> builder
                .topicArn(topic.topicArn())
                .subject("Products import status report")
                .message("Successfully imported products from .csv file")
        );
    }
}
