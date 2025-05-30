package com.yookassa.spring.events;

import com.yookassa.spring.domain.Payment;
import com.yookassa.spring.domain.PaymentStatus;
import lombok.*;


@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class PaymentStatusChangedEvent extends PaymentEvent {

    private final PaymentStatus previousStatus;
    private final PaymentStatus newStatus;

    public PaymentStatusChangedEvent(Payment payment, PaymentStatus previousStatus, PaymentStatus newStatus) {
        super(payment);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}