package com.yookassa.spring.handlers;

import com.yookassa.spring.events.PaymentCanceledEvent;
import com.yookassa.spring.events.PaymentStatusChangedEvent;
import com.yookassa.spring.events.PaymentSucceededEvent;
import com.yookassa.spring.events.PaymentWaitingForCaptureEvent;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class PaymentHandlerRegistry {

    private final PaymentEventHandler eventHandler;

    public PaymentHandlerRegistry(PaymentEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public PaymentHandlerRegistry onPaymentSucceeded(Consumer<PaymentSucceededEvent> handler) {
        eventHandler.registerHandler(PaymentSucceededEvent.class,
                event -> handler.accept((PaymentSucceededEvent) event));
        return this;
    }

    public PaymentHandlerRegistry onPaymentCanceled(Consumer<PaymentCanceledEvent> handler) {
        eventHandler.registerHandler(PaymentCanceledEvent.class,
                event -> handler.accept((PaymentCanceledEvent) event));
        return this;
    }

    public PaymentHandlerRegistry onPaymentWaitingForCapture(Consumer<PaymentWaitingForCaptureEvent> handler) {
        eventHandler.registerHandler(PaymentWaitingForCaptureEvent.class,
                event -> handler.accept((PaymentWaitingForCaptureEvent) event));
        return this;
    }

    public PaymentHandlerRegistry onPaymentStatusChanged(Consumer<PaymentStatusChangedEvent> handler) {
        eventHandler.registerHandler(PaymentStatusChangedEvent.class,
                event -> handler.accept((PaymentStatusChangedEvent) event));
        return this;
    }
}