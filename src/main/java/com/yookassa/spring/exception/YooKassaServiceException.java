package com.yookassa.spring.exception;

public class YooKassaServiceException extends YooKassaException {

    public YooKassaServiceException(String message) {
        super(message);
    }

    public YooKassaServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}