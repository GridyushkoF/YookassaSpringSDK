package com.yookassa.spring.events;

import com.yookassa.spring.domain.Payment;

public class PaymentWaitingForCaptureEvent extends PaymentEvent {
    public PaymentWaitingForCaptureEvent(Payment payment) {
        super(payment);
    }
}