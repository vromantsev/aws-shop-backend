package ua.reed.exceptions;

public class FailedToSendSqsMessagesException extends RuntimeException {

    public FailedToSendSqsMessagesException() {
    }

    public FailedToSendSqsMessagesException(String message) {
        super(message);
    }

    public FailedToSendSqsMessagesException(String message, Throwable cause) {
        super(message, cause);
    }
}
