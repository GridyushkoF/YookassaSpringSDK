package com.yookassa.spring.webhook;

import com.yookassa.spring.domain.Payment;
import com.yookassa.spring.events.PaymentCanceledEvent;
import com.yookassa.spring.events.PaymentEvent;
import com.yookassa.spring.events.PaymentSucceededEvent;
import com.yookassa.spring.events.PaymentWaitingForCaptureEvent;
import com.yookassa.spring.util.PaymentUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("${yookassa.webhook.path:/api/webhooks/yookassa}")
@Slf4j
public class WebhookController {

    private final WebhookProcessor webhookProcessor;
    private final ApplicationEventPublisher eventPublisher;

    public WebhookController(WebhookProcessor webhookProcessor, ApplicationEventPublisher eventPublisher) {
        this.webhookProcessor = webhookProcessor;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String requestBody,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request) {

        try {
            // Валидация webhook
            if (!webhookProcessor.validateWebhook(requestBody, headers, request)) {
                log.warn("Invalid webhook received from IP: {}", PaymentUtils.getClientIp(request));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Обработка webhook
            WebhookNotification notification = webhookProcessor.processWebhook(requestBody);

            // Публикация события
            PaymentEvent event = createEventFromNotification(notification);
            if (event != null) {
                eventPublisher.publishEvent(event);
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private PaymentEvent createEventFromNotification(WebhookNotification notification) {
        Payment payment = notification.getObject();

        switch (notification.getEvent()) {
            case "payment.succeeded":
                return new PaymentSucceededEvent(payment);
            case "payment.waiting_for_capture":
                return new PaymentWaitingForCaptureEvent(payment);
            case "payment.canceled":
                return new PaymentCanceledEvent(payment, "webhook_notification");
            default:
                log.warn("Unknown event type: {}", notification.getEvent());
                return null;
        }
    }



}