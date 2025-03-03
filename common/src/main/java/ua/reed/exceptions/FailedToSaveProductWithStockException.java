package ua.reed.exceptions;

public class FailedToSaveProductWithStockException extends RuntimeException {

    public FailedToSaveProductWithStockException() {
    }

    public FailedToSaveProductWithStockException(String message) {
        super(message);
    }

    public FailedToSaveProductWithStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
