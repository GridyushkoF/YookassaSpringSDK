package com.yookassa.spring.events;

import com.yookassa.spring.domain.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public abstract class PaymentEvent {
    private final Payment payment;
    private final LocalDateTime timestamp;

    public PaymentEvent(Payment payment) {
        this.payment = payment;
        this.timestamp = LocalDateTime.now();
    }
}