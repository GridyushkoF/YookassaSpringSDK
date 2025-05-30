package com.yookassa.spring.events;

import com.yookassa.spring.domain.Payment;
import com.yookassa.spring.domain.Refund;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class RefundSucceededEvent extends PaymentEvent {
    private final Refund refund;

    public RefundSucceededEvent(Payment payment, Refund refund) {
        super(payment);
        this.refund = refund;
    }
}