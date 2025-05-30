package com.yookassa.spring.webhook;

import com.yookassa.spring.domain.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookNotification {
    private String type;
    private String event;
    private Payment object;
}