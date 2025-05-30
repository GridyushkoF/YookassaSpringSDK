package com.yookassa.spring.exception;
public class PaymentProcessingException extends YooKassaException {

    private final String paymentId;

    public PaymentProcessingException(String message) {
        super(message);
        this.paymentId = null;
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.paymentId = null;
    }

    public PaymentProcessingException(String message, String paymentId) {
        super(message);
        this.paymentId = paymentId;
    }

    public PaymentProcessingException(String message, String paymentId, Throwable cause) {
        super(message, cause);
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }
}