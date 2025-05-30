package com.yookassa.spring.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yookassa")
@Data
public class YooKassaProperties {

    private String shopId;
    private String secretKey;
    private String apiUrl = "https://api.yookassa.ru";

    private Webhook webhook = new Webhook();
    private Scheduler scheduler = new Scheduler();

    @Data
    public static class Webhook {
        private String path = "/api/webhooks/yookassa";
        private boolean signatureValidationEnabled = true;
        private String secretKey;
    }

    @Data
    public static class Scheduler {
        private boolean enabled = true;
        private long checkInterval = 60000; // 1 минута
        private long delayBetweenRequests = 1000; // 1 секунда
    }
}