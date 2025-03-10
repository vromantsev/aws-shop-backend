package ua.reed.dto;

public record ImportFileResponse(int statusCode, String message) {

    public static ImportFileResponse withInternalServerError() {
        return new ImportFileResponse(500, "{\"message\":\"Internal Server Error\"}");
    }
}
