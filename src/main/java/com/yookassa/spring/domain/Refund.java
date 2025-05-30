package com.yookassa.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {
    private String id;
    private String paymentId;
    private RefundStatus status;
    private Amount amount;
    private String description;
    private String createdAt;
    private String reason;
}
