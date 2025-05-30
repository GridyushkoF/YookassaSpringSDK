package com.yookassa.spring.events;

import com.yookassa.spring.domain.Payment;

public class PaymentExpiredEvent extends PaymentEvent {
    public PaymentExpiredEvent(Payment payment) {
        super(payment);
    }
}