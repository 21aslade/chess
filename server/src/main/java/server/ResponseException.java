package server;

public class ResponseException extends Exception {
    private final int httpCode;

    public ResponseException(int httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }

    public int httpCode() {
        return this.httpCode;
    }
}
