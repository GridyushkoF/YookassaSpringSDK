package com.yookassa.spring.util;

import com.yookassa.spring.domain.PaymentStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.UUID;

@UtilityClass
public class PaymentUtils {

    public static boolean isPaymentCompleted(PaymentStatus status) {
        return status == PaymentStatus.SUCCEEDED || status == PaymentStatus.CANCELED;
    }

    public static boolean isPaymentPending(PaymentStatus status) {
        return status == PaymentStatus.PENDING || status == PaymentStatus.WAITING_FOR_CAPTURE;
    }

    public static String generateIdempotenceKey() {
        return UUID.randomUUID().toString();
    }

    public static String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public static boolean isValidCurrency(String currency) {
        return Arrays.asList("RUB", "USD", "EUR").contains(currency.toUpperCase());
    }

    public static String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}