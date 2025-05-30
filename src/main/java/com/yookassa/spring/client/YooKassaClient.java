package com.yookassa.spring.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yookassa.spring.autoconfigure.YooKassaProperties;
import com.yookassa.spring.domain.Payment;
import com.yookassa.spring.exception.YooKassaApiException;
import com.yookassa.spring.exception.YooKassaErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.UUID;

@Slf4j
public class YooKassaClient {

    private final RestTemplate restTemplate;
    private final YooKassaProperties properties;

    public YooKassaClient(YooKassaProperties properties,
                          @Qualifier("yooKassaRestTemplate") RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }
    public Payment createPayment(CreatePaymentRequest request) {
        try {
            String idempotenceKey = UUID.randomUUID().toString();

            HttpHeaders headers = createHeaders();
            headers.set("Idempotence-Key", idempotenceKey);

            HttpEntity<CreatePaymentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Payment> response = restTemplate.exchange(
                    properties.getApiUrl() + "/v3/payments",
                    HttpMethod.POST,
                    entity,
                    Payment.class
            );

            Payment payment = response.getBody();
            log.info("Payment created: id={}, status={}", payment.getId(), payment.getStatus());

            return payment;

        } catch (Exception e) {
            log.error("Failed to create payment", e);
            throw new YooKassaApiException("Failed to create payment", e);
        }
    }

    public Payment getPayment(String paymentId) {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Payment> response = restTemplate.exchange(
                    properties.getApiUrl() + "/v3/payments/" + paymentId,
                    HttpMethod.GET,
                    entity,
                    Payment.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get payment: {}", paymentId, e);
            throw new YooKassaApiException("Failed to get payment", e);
        }
    }

    public Payment capturePayment(String paymentId, CapturePaymentRequest request) {
        try {
            String idempotenceKey = UUID.randomUUID().toString();

            HttpHeaders headers = createHeaders();
            headers.set("Idempotence-Key", idempotenceKey);

            HttpEntity<CapturePaymentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Payment> response = restTemplate.exchange(
                    properties.getApiUrl() + "/v3/payments/" + paymentId + "/capture",
                    HttpMethod.POST,
                    entity,
                    Payment.class
            );

            Payment payment = response.getBody();
            log.info("Payment captured: id={}, status={}", payment.getId(), payment.getStatus());

            return payment;

        } catch (Exception e) {
            log.error("Failed to capture payment: {}", paymentId, e);
            throw new YooKassaApiException("Failed to capture payment", e);
        }
    }

    public Payment cancelPayment(String paymentId) {
        try {
            String idempotenceKey = UUID.randomUUID().toString();

            HttpHeaders headers = createHeaders();
            headers.set("Idempotence-Key", idempotenceKey);

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Payment> response = restTemplate.exchange(
                    properties.getApiUrl() + "/v3/payments/" + paymentId + "/cancel",
                    HttpMethod.POST,
                    entity,
                    Payment.class
            );

            Payment payment = response.getBody();
            log.info("Payment canceled: id={}, status={}", payment.getId(), payment.getStatus());

            return payment;

        } catch (Exception e) {
            log.error("Failed to cancel payment: {}", paymentId, e);
            throw new YooKassaApiException("Failed to cancel payment", e);
        }
    }

    private RestTemplate createRestTemplate() {
        RestTemplate template = new RestTemplate();

        // Добавляем обработчик ошибок
        template.setErrorHandler(new YooKassaErrorHandler());

        // Добавляем интерсептор для логирования
        template.getInterceptors().add(new LoggingInterceptor());

        return template;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = properties.getShopId() + ":" + properties.getSecretKey();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        return headers;
    }
}