package com.yookassa.spring.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class YooKassaErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() ||
                response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        int statusCode = response.getStatusCode().value();

        log.error("YooKassa API error: status={}, body={}", statusCode, responseBody);

        try {
            YooKassaErrorResponse errorResponse = objectMapper.readValue(responseBody, YooKassaErrorResponse.class);
            throw new YooKassaApiException(
                    errorResponse.getDescription(),
                    statusCode,
                    errorResponse.getCode()
            );
        } catch (JsonProcessingException e) {
            throw new YooKassaApiException(
                    "HTTP " + statusCode + ": " + responseBody,
                    statusCode,
                    null
            );
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YooKassaErrorResponse {
        private String type;
        private String id;
        private String code;
        private String description;
        private String parameter;
    }
}