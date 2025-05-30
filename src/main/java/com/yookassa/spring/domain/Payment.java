package com.yookassa.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private String id;
    private PaymentStatus status;
    private Amount amount;
    private String description;
    private PaymentMethodData paymentMethodData;
    private Confirmation confirmation;
    private Boolean capture;
    private String createdAt;
    private String expiresAt;
    private Metadata metadata;
    private Boolean test;
    private Boolean paid;
    private Boolean refundable;
    private String receiptRegistration;
}