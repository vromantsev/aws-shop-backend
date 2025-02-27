package ua.reed.exceptions;

public class FailedToGetProductsException extends RuntimeException {

    public FailedToGetProductsException() {
    }

    public FailedToGetProductsException(String message) {
        super(message);
    }

    public FailedToGetProductsException(String message, Throwable cause) {
        super(message, cause);
    }
}
