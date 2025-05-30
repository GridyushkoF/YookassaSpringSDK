package com.yookassa.spring.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yookassa.spring.YooKassaService;
import com.yookassa.spring.client.IPValidator;
import com.yookassa.spring.client.YooKassaClient;
import com.yookassa.spring.handlers.PaymentEventHandler;
import com.yookassa.spring.handlers.PaymentHandlerRegistry;
import com.yookassa.spring.scheduler.InMemoryPaymentStatusRepository;
import com.yookassa.spring.scheduler.PaymentStatusRepository;
import com.yookassa.spring.scheduler.PaymentStatusScheduler;
import com.yookassa.spring.scheduler.PaymentStatusService;
import com.yookassa.spring.validation.PaymentValidator;
import com.yookassa.spring.webhook.WebhookController;
import com.yookassa.spring.webhook.WebhookProcessor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(YooKassaProperties.class)
@ConditionalOnProperty(prefix = "yookassa", name = "shop-id")
@EnableScheduling
@Import(YooKassaRestTemplateConfig.class) // Добавьте этот импорт
@Slf4j
public class YooKassaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IPValidator ipValidator() {
        return new IPValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentStatusRepository paymentStatusRepository() {
        return new InMemoryPaymentStatusRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentStatusService paymentStatusService(PaymentStatusRepository repository) {
        return new PaymentStatusService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentValidator paymentValidator() {
        return new PaymentValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public YooKassaClient yooKassaClient(YooKassaProperties properties, RestTemplate restTemplate) {
        return new YooKassaClient(properties, restTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public YooKassaService yooKassaService(YooKassaClient client,
                                           PaymentStatusService paymentStatusService,
                                           ApplicationEventPublisher eventPublisher,
                                           PaymentValidator paymentValidator) {
        return new YooKassaService(client, paymentStatusService, eventPublisher, paymentValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentEventHandler paymentEventHandler() {
        return new PaymentEventHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentHandlerRegistry paymentHandlerRegistry(PaymentEventHandler eventHandler) {
        return new PaymentHandlerRegistry(eventHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebhookProcessor webhookProcessor(YooKassaProperties properties,
                                             ObjectMapper objectMapper,
                                             IPValidator ipValidator) {
        return new WebhookProcessor(properties, objectMapper, ipValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebhookController webhookController(WebhookProcessor webhookProcessor,
                                               ApplicationEventPublisher eventPublisher) {
        return new WebhookController(webhookProcessor, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentStatusScheduler paymentStatusScheduler(PaymentStatusService paymentStatusService,
                                                         YooKassaClient yooKassaClient,
                                                         ApplicationEventPublisher eventPublisher,
                                                         YooKassaProperties properties) {
        return new PaymentStatusScheduler(paymentStatusService, yooKassaClient, eventPublisher, properties);
    }

    @PostConstruct
    public void logConfiguration() {
        log.info("YooKassa Spring Boot Starter initialized");
    }
}
