package com.yookassa.spring.events;

import com.yookassa.spring.domain.Payment;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@Getter
@EqualsAndHashCode(callSuper = true)
public class PaymentCanceledEvent extends PaymentEvent {
    private final String reason;

    public PaymentCanceledEvent(Payment payment, String reason) {
        super(payment);
        this.reason = reason;
    }
}