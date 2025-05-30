package com.yookassa.spring.client;

import com.yookassa.spring.domain.Amount;
import com.yookassa.spring.domain.Confirmation;
import com.yookassa.spring.domain.Metadata;
import com.yookassa.spring.domain.PaymentMethodData;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    @NotNull
    private Amount amount;

    private String description;
    private PaymentMethodData paymentMethodData;
    private Confirmation confirmation;
    private Boolean capture;
    private Metadata metadata;
}