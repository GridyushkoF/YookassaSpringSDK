package com.yookassa.spring.client;

import com.yookassa.spring.domain.Amount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapturePaymentRequest {
    private Amount amount;
}