package com.yookassa.spring.scheduler;

import com.yookassa.spring.autoconfigure.YooKassaProperties;
import com.yookassa.spring.client.YooKassaClient;
import com.yookassa.spring.domain.Payment;
import com.yookassa.spring.domain.PaymentStatus;
import com.yookassa.spring.events.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(value = "yookassa.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentStatusScheduler {

    private final PaymentStatusService paymentStatusService;
    private final YooKassaClient yooKassaClient;
    private final ApplicationEventPublisher eventPublisher;
    private final YooKassaProperties properties;

    public PaymentStatusScheduler(PaymentStatusService paymentStatusService,
                                  YooKassaClient yooKassaClient,
                                  ApplicationEventPublisher eventPublisher,
                                  YooKassaProperties properties) {
        this.paymentStatusService = paymentStatusService;
        this.yooKassaClient = yooKassaClient;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${yookassa.scheduler.check-interval:60000}")
    public void checkPaymentStatuses() {
        if (!properties.getScheduler().isEnabled()) {
            return;
        }

        log.debug("Starting payment status check");

        try {
            List<String> pendingPaymentIds = paymentStatusService.getPendingPaymentIds();

            for (String paymentId : pendingPaymentIds) {
                try {
                    checkPaymentStatus(paymentId);
                    Thread.sleep(properties.getScheduler().getDelayBetweenRequests());
                } catch (Exception e) {
                    log.error("Error checking payment status: {}", paymentId, e);
                }
            }

        } catch (Exception e) {
            log.error("Error in payment status scheduler", e);
        }

        log.debug("Payment status check completed");
    }

    private void checkPaymentStatus(String paymentId) {
        try {
            Payment currentPayment = yooKassaClient.getPayment(paymentId);
            PaymentStatus previousStatus = paymentStatusService.getLastKnownStatus(paymentId);

            if (previousStatus != currentPayment.getStatus()) {
                log.info("Payment status changed: id={}, {} -> {}",
                        paymentId, previousStatus, currentPayment.getStatus());

                // Обновляем статус в локальном хранилище
                paymentStatusService.updatePaymentStatus(paymentId, currentPayment.getStatus());

                // Публикуем событие об изменении статуса
                PaymentStatusChangedEvent event = new PaymentStatusChangedEvent(
                        currentPayment, previousStatus, currentPayment.getStatus());
                eventPublisher.publishEvent(event);

                // Публикуем специфичное событие в зависимости от нового статуса
                PaymentEvent specificEvent = createSpecificEvent(currentPayment);
                if (specificEvent != null) {
                    eventPublisher.publishEvent(specificEvent);
                }
            }

        } catch (Exception e) {
            log.error("Failed to check payment status: {}", paymentId, e);
        }
    }

    private PaymentEvent createSpecificEvent(Payment payment) {
        switch (payment.getStatus()) {
            case SUCCEEDED:
                return new PaymentSucceededEvent(payment);
            case WAITING_FOR_CAPTURE:
                return new PaymentWaitingForCaptureEvent(payment);
            case CANCELED:
                return new PaymentCanceledEvent(payment, "status_check");
            default:
                return null;
        }
    }
}