package com.yookassa.spring;

import com.yookassa.spring.domain.Metadata;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull
    private BigDecimal amount;

    @NotNull
    private String currency;

    private String description;
    private String returnUrl;
    private boolean capture;
    private Metadata metadata;
}