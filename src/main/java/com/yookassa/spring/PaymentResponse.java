package com.yookassa.spring;

import com.yookassa.spring.domain.Amount;
import com.yookassa.spring.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private PaymentStatus status;
    private String confirmationUrl;
    private Amount amount;
}