package se.cgbystrom.netty.http.rest;

public class RestException extends RuntimeException {
    private int statusCode;

    public RestException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
