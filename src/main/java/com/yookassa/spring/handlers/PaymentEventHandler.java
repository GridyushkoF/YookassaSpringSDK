package com.yookassa.spring.handlers;

import com.yookassa.spring.domain.Payment;
import com.yookassa.spring.events.PaymentCanceledEvent;
import com.yookassa.spring.events.PaymentEvent;
import com.yookassa.spring.events.PaymentSucceededEvent;
import com.yookassa.spring.events.PaymentWaitingForCaptureEvent;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@Slf4j
public class PaymentEventHandler {

    private final Map<Class<? extends PaymentEvent>, List<Consumer<PaymentEvent>>> handlers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Регистрация обработчиков по умолчанию
        registerHandler(PaymentSucceededEvent.class, this::handlePaymentSucceeded);
        registerHandler(PaymentCanceledEvent.class, this::handlePaymentCanceled);
        registerHandler(PaymentWaitingForCaptureEvent.class, this::handlePaymentWaitingForCapture);
    }

    public <T extends PaymentEvent> void registerHandler(Class<T> eventType, Consumer<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new ArrayList<>())
                .add((Consumer<PaymentEvent>) handler);
    }

    @EventListener
    public void handlePaymentEvent(PaymentEvent event) {
        List<Consumer<PaymentEvent>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            eventHandlers.forEach(handler -> {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    log.error("Error processing payment event: {}", event.getClass().getSimpleName(), e);
                }
            });
        }
    }

    private void handlePaymentSucceeded(PaymentEvent event) {
        Payment payment = event.getPayment();
        log.info("Payment succeeded: id={}, amount={}",
                payment.getId(), payment.getAmount().getValue());
    }

    private void handlePaymentCanceled(PaymentEvent event) {
        PaymentCanceledEvent canceledEvent = (PaymentCanceledEvent) event;
        log.info("Payment canceled: id={}, reason={}",
                canceledEvent.getPayment().getId(), canceledEvent.getReason());
    }

    private void handlePaymentWaitingForCapture(PaymentEvent event) {
        Payment payment = event.getPayment();
        log.info("Payment waiting for capture: id={}", payment.getId());
    }
}

