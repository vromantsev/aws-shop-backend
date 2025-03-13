package ua.reed.dto;

public record DeleteS3ObjectResponse(int statusCode, String message, boolean hasErrors) {
    
    public static DeleteS3ObjectResponse of(int statusCode, String message) {
        return new DeleteS3ObjectResponse(statusCode, message, false);
    }
    
    public static DeleteS3ObjectResponse withError(String message) {
        return new DeleteS3ObjectResponse(500, message, true);
    }
}
