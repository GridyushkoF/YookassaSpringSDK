package com.yookassa.spring.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yookassa.spring.autoconfigure.YooKassaProperties;
import com.yookassa.spring.client.IPValidator;
import com.yookassa.spring.util.PaymentUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

@Slf4j
public class WebhookProcessor {

    private final YooKassaProperties properties;
    private final ObjectMapper objectMapper;
    private final IPValidator ipValidator;

    public WebhookProcessor(YooKassaProperties properties, ObjectMapper objectMapper, IPValidator ipValidator) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.ipValidator = ipValidator;
    }

    public boolean validateWebhook(String requestBody, Map<String, String> headers, HttpServletRequest request) {
        // Проверка IP-адреса
        if (!ipValidator.isValidYooKassaIP(getClientIP(request))) {
            return false;
        }

        // Проверка подписи (если включена)
        if (properties.getWebhook().isSignatureValidationEnabled()) {
            String signature = headers.get("webhook-signature");
            return validateSignature(requestBody, signature);
        }

        return true;
    }

    public WebhookNotification processWebhook(String requestBody) throws JsonProcessingException {
        return objectMapper.readValue(requestBody, WebhookNotification.class);
    }

    private boolean validateSignature(String requestBody, String signature) {
        if (signature == null || properties.getWebhook().getSecretKey() == null) {
            return false;
        }

        try {
            String expectedSignature = calculateHMAC(requestBody, properties.getWebhook().getSecretKey());
            return signature.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Error validating signature", e);
            return false;
        }
    }

    private String calculateHMAC(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    public String getClientIP(HttpServletRequest request) {
        return PaymentUtils.getClientIp(request);
    }

}