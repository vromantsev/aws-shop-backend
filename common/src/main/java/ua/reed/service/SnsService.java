package ua.reed.service;

import software.amazon.awssdk.services.sns.model.Topic;

import java.util.Optional;

public interface SnsService {

    boolean doesTopicExist(String topicName);

    Optional<Topic> getTopicByName(String topicName);

    void sendEmailNotification();

}
