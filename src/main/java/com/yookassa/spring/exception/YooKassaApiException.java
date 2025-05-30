package com.yookassa.spring.exception;
public class YooKassaApiException extends YooKassaException {

    private final int statusCode;
    private final String errorCode;

    public YooKassaApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorCode = null;
    }

    public YooKassaApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorCode = null;
    }

    public YooKassaApiException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}