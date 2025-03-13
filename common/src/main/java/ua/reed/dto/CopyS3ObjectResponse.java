package ua.reed.dto;

public record CopyS3ObjectResponse(int statusCode, String message, boolean isSuccessful) {

    public static CopyS3ObjectResponse ofSuccess(int statusCode, String message) {
        return new CopyS3ObjectResponse(statusCode, message, true);
    }

    public static CopyS3ObjectResponse ofFailure(String message) {
        return new CopyS3ObjectResponse(500, message, false);
    }

    public static CopyS3ObjectResponse of(int statusCode, String message, boolean isSuccessful) {
        return new CopyS3ObjectResponse(statusCode, message, isSuccessful);
    }
}
