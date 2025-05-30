package com.yookassa.spring.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

// exception/YooKassaExceptionHandler.java
@ControllerAdvice
@Slf4j
public class YooKassaExceptionHandler {

    @ExceptionHandler(YooKassaApiException.class)
    public ResponseEntity<ErrorResponse> handleYooKassaApiException(YooKassaApiException e) {
        log.error("YooKassa API error: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("YOOKASSA_API_ERROR")
                .message(e.getMessage())
                .statusCode(e.getStatusCode())
                .errorCode(e.getErrorCode())
                .timestamp(LocalDateTime.now())
                .build();

        HttpStatus status = e.getStatusCode() > 0
                ? HttpStatus.valueOf(e.getStatusCode())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessingException(PaymentProcessingException e) {
        log.error("Payment processing error: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("PAYMENT_PROCESSING_ERROR")
                .message(e.getMessage())
                .paymentId(e.getPaymentId())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(YooKassaServiceException.class)
    public ResponseEntity<ErrorResponse> handleYooKassaServiceException(YooKassaServiceException e) {
        log.error("YooKassa service error: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("YOOKASSA_SERVICE_ERROR")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String message;
        private Integer statusCode;
        private String errorCode;
        private String paymentId;
        private LocalDateTime timestamp;
    }
}
