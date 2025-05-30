package com.yookassa.spring.client;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        logRequest(request, body);

        ClientHttpResponse response = execution.execute(request, body);

        logResponse(response);

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        if (log.isDebugEnabled()) {
            log.debug("YooKassa API Request: {} {}", request.getMethod(), request.getURI());
            log.debug("Request headers: {}", request.getHeaders());

            if (body.length > 0) {
                String requestBody = new String(body, StandardCharsets.UTF_8);
                // Маскируем чувствительные данные
                String maskedBody = maskSensitiveData(requestBody);
                log.debug("Request body: {}", maskedBody);
            }
        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("YooKassa API Response: {}", response.getStatusCode());
            log.debug("Response headers: {}", response.getHeaders());
        }
    }

    private String maskSensitiveData(String data) {
        // Маскируем секретные ключи и другие чувствительные данные
        return data.replaceAll("\"secret_key\"\\s*:\\s*\"[^\"]+\"", "\"secret_key\":\"***\"")
                .replaceAll("\"password\"\\s*:\\s*\"[^\"]+\"", "\"password\":\"***\"");
    }
}