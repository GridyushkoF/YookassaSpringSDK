package com.yookassa.spring.events;

import com.yookassa.spring.domain.Payment;

public class PaymentSucceededEvent extends PaymentEvent {
    public PaymentSucceededEvent(Payment payment) {
        super(payment);
    }
}