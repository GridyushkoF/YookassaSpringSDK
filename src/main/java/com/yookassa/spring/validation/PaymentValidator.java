package com.yookassa.spring.validation;

import com.yookassa.spring.PaymentRequest;
import com.yookassa.spring.util.PaymentUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentValidator {

    public void validatePaymentRequest(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (request.getAmount().compareTo(new BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds maximum limit");
        }

        if (!PaymentUtils.isValidCurrency(request.getCurrency())) {
            throw new IllegalArgumentException("Unsupported currency: " + request.getCurrency());
        }

        if (request.getDescription() != null && request.getDescription().length() > 128) {
            throw new IllegalArgumentException("Description too long (max 128 characters)");
        }
    }

    public void validateCaptureRequest(String paymentId, BigDecimal amount) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be empty");
        }

        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Capture amount must be positive");
        }
    }
}
