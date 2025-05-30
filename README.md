

# YooKassa Spring Boot Starter

[
[
[
[

Полнофункциональная Spring Boot библиотека для интеграции с платежной системой ЮKassa (YooMoney). Предоставляет удобный API для создания платежей, обработки webhook-уведомлений и автоматического мониторинга статусов платежей.

## 🚀 Основные возможности

- **Простая интеграция** - автоматическая конфигурация через Spring Boot
- **Создание платежей** - генерация URL для оплаты одной строкой кода
- **Event-driven архитектура** - обработка событий через Spring Events и Lambda API
- **Автоматический мониторинг** - встроенный scheduler для отслеживания изменений статусов
- **Безопасные webhook** - валидация IP-адресов и подписей от ЮKassa
- **Гибкая конфигурация** - настройка через application.properties/yml
- **Обработка ошибок** - детальная обработка исключений API
- **Логирование** - подробные логи всех операций


## 📦 Установка

### Gradle

```gradle
dependencies {
    implementation 'com.yookassa.spring:yookassa-spring-boot-starter:1.0.0'
}
```


### Maven

```xml
<dependency>
    <groupId>com.yookassa.spring</groupId>
    <artifactId>yookassa-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```


## ⚙️ Конфигурация

Добавьте настройки в `application.yml`:

```yaml
yookassa:
  shop-id: ${YOOKASSA_SHOP_ID}
  secret-key: ${YOOKASSA_SECRET_KEY}
  api-url: https://api.yookassa.ru
  
  webhook:
    path: /api/webhooks/yookassa
    signature-validation-enabled: true
    secret-key: ${YOOKASSA_WEBHOOK_SECRET}
  
  scheduler:
    enabled: true
    check-interval: 60000  # 1 минута
    delay-between-requests: 1000  # 1 секунда между запросами
```

Или в `application.properties`:

```properties
yookassa.shop-id=${YOOKASSA_SHOP_ID}
yookassa.secret-key=${YOOKASSA_SECRET_KEY}
yookassa.webhook.path=/api/webhooks/yookassa
yookassa.scheduler.enabled=true
```


## 🔧 Быстрый старт

### 1. Создание платежа

```java
@RestController
public class PaymentController {
    
    @Autowired
    private YooKassaService yooKassaService;
    
    @PostMapping("/create-payment")
    public ResponseEntity<String> createPayment() {
        PaymentRequest request = PaymentRequest.builder()
            .amount(new BigDecimal("1000.00"))
            .currency("RUB")
            .description("Оплата заказа #12345")
            .returnUrl("https://example.com/success")
            .capture(true)
            .build();
        
        String paymentUrl = yooKassaService.generatePaymentUrl(request);
        return ResponseEntity.ok(paymentUrl);
    }
}
```


### 2. Обработка событий платежей

```java
@Component
public class PaymentEventListener {
    
    @Autowired
    private PaymentHandlerRegistry handlerRegistry;
    
    @PostConstruct
    public void setupHandlers() {
        handlerRegistry
            .onPaymentSucceeded(event -> {
                Payment payment = event.getPayment();
                log.info("✅ Платеж успешен: {} на сумму {}", 
                        payment.getId(), payment.getAmount().getValue());
                // Обновить статус заказа в базе данных
                orderService.markAsPaid(payment.getId());
            })
            .onPaymentCanceled(event -> {
                Payment payment = event.getPayment();
                log.warn("❌ Платеж отменен: {}", payment.getId());
                // Отменить заказ
                orderService.cancel(payment.getId());
            })
            .onPaymentWaitingForCapture(event -> {
                Payment payment = event.getPayment();
                log.info("⏳ Платеж ожидает подтверждения: {}", payment.getId());
                // Автоматическое подтверждение
                yooKassaService.capturePayment(payment.getId(), payment.getAmount().getValue());
            });
    }
}
```


### 3. Использование Spring Events

```java
@Component
@Slf4j
public class OrderService {
    
    @EventListener
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        Payment payment = event.getPayment();
        String orderId = payment.getMetadata().getOrderId();
        
        // Обновляем статус заказа
        updateOrderStatus(orderId, OrderStatus.PAID);
        
        // Отправляем уведомление клиенту
        emailService.sendPaymentConfirmation(orderId);
        
        log.info("Заказ {} успешно оплачен", orderId);
    }
    
    @EventListener
    public void handlePaymentStatusChanged(PaymentStatusChangedEvent event) {
        log.info("Статус платежа {} изменился: {} -> {}", 
                event.getPayment().getId(),
                event.getPreviousStatus(),
                event.getNewStatus());
    }
}
```


## 📋 Подробная документация

### Создание платежей

#### Простой платеж

```java
PaymentRequest request = PaymentRequest.builder()
    .amount(new BigDecimal("500.00"))
    .currency("RUB")
    .description("Покупка товара")
    .returnUrl("https://shop.example.com/success")
    .capture(true)  // Автоматическое списание
    .build();

PaymentResponse response = yooKassaService.createPayment(request);
```


#### Платеж с метаданными

```java
Metadata metadata = Metadata.builder()
    .orderId("ORDER-12345")
    .userId("USER-67890")
    .customFields(Map.of(
        "product_id", "PROD-001",
        "promo_code", "SUMMER2025"
    ))
    .build();

PaymentRequest request = PaymentRequest.builder()
    .amount(new BigDecimal("1500.00"))
    .currency("RUB")
    .description("Заказ в интернет-магазине")
    .returnUrl("https://shop.example.com/payment/success")
    .capture(false)  // Двухстадийный платеж
    .metadata(metadata)
    .build();
```


### Управление платежами

#### Подтверждение платежа

```java
// Подтверждение на полную сумму
Payment payment = yooKassaService.capturePayment(paymentId, null);

// Частичное подтверждение
Payment payment = yooKassaService.capturePayment(paymentId, new BigDecimal("750.00"));
```


#### Отмена платежа

```java
Payment canceledPayment = yooKassaService.cancelPayment(paymentId);
```


#### Получение информации о платеже

```java
Payment payment = yooKassaService.getPaymentInfo(paymentId);
log.info("Статус платежа: {}", payment.getStatus());
```


### Обработка webhook

Библиотека автоматически обрабатывает webhook-уведомления от ЮKassa. Webhook-контроллер доступен по пути, указанному в конфигурации (по умолчанию `/api/webhooks/yookassa`).

#### Типы событий

| Событие | Описание | Класс события |
| :-- | :-- | :-- |
| `payment.succeeded` | Успешная оплата | `PaymentSucceededEvent` |
| `payment.waiting_for_capture` | Ожидает подтверждения | `PaymentWaitingForCaptureEvent` |
| `payment.canceled` | Платеж отменен | `PaymentCanceledEvent` |
| `refund.succeeded` | Успешный возврат | `RefundSucceededEvent` |

#### Безопасность webhook

Библиотека автоматически проверяет:

- **IP-адреса** - только запросы с официальных IP ЮKassa
- **Подписи** - HMAC-SHA256 подпись запроса (если включена)


### Автоматический мониторинг

Встроенный scheduler периодически проверяет статусы платежей и генерирует события при их изменении.

#### Настройка scheduler

```yaml
yookassa:
  scheduler:
    enabled: true
    check-interval: 30000  # Проверка каждые 30 секунд
    delay-between-requests: 500  # Задержка между запросами к API
```


#### Отключение scheduler

```yaml
yookassa:
  scheduler:
    enabled: false
```


### Обработка ошибок

Библиотека предоставляет детальную обработку ошибок:

```java
try {
    PaymentResponse response = yooKassaService.createPayment(request);
} catch (YooKassaApiException e) {
    log.error("Ошибка API ЮKassa: {} (код: {})", e.getMessage(), e.getStatusCode());
} catch (PaymentProcessingException e) {
    log.error("Ошибка обработки платежа {}: {}", e.getPaymentId(), e.getMessage());
} catch (YooKassaServiceException e) {
    log.error("Ошибка сервиса: {}", e.getMessage());
}
```


## 🔧 Расширенная конфигурация

### Кастомный репозиторий статусов

По умолчанию используется in-memory хранилище. Для продакшена рекомендуется реализовать собственный репозиторий:

```java
@Component
@Primary
public class DatabasePaymentStatusRepository implements PaymentStatusRepository {
    
    @Autowired
    private PaymentStatusJpaRepository jpaRepository;
    
    @Override
    public List<String> findPendingPaymentIds() {
        return jpaRepository.findByStatusIn(
            Arrays.asList(PaymentStatus.PENDING, PaymentStatus.WAITING_FOR_CAPTURE)
        ).stream()
        .map(PaymentStatusEntity::getPaymentId)
        .collect(Collectors.toList());
    }
    
    // Остальные методы...
}
```


### Кастомная обработка событий

```java
@Component
public class CustomPaymentHandler {
    
    @EventListener
    @Async
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        // Асинхронная обработка успешного платежа
        CompletableFuture.runAsync(() -> {
            processSuccessfulPayment(event.getPayment());
        });
    }
    
    @EventListener
    @Order(1) // Приоритет обработки
    public void handlePaymentCanceled(PaymentCanceledEvent event) {
        // Высокоприоритетная обработка отмены
        urgentCancellationProcessing(event.getPayment());
    }
}
```


## 🧪 Тестирование

### Unit тесты

```java
@SpringBootTest
@TestPropertySource(properties = {
    "yookassa.shop-id=test-shop",
    "yookassa.secret-key=test-secret",
    "yookassa.scheduler.enabled=false"
})
class PaymentServiceTest {
    
    @Autowired
    private YooKassaService yooKassaService;
    
    @MockBean
    private YooKassaClient yooKassaClient;
    
    @Test
    void shouldCreatePayment() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .currency("RUB")
            .build();
        
        Payment mockPayment = Payment.builder()
            .id("test-payment-id")
            .status(PaymentStatus.PENDING)
            .build();
        
        when(yooKassaClient.createPayment(any())).thenReturn(mockPayment);
        
        // Act
        PaymentResponse response = yooKassaService.createPayment(request);
        
        // Assert
        assertThat(response.getPaymentId()).isEqualTo("test-payment-id");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }
}
```


### Интеграционные тесты

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "yookassa.shop-id=test-shop",
    "yookassa.secret-key=test-secret"
})
class WebhookIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @LocalServerPort
    private int port;
    
    @Test
    void shouldProcessWebhook() {
        String webhookPayload = """
            {
                "type": "notification",
                "event": "payment.succeeded",
                "object": {
                    "id": "test-payment-id",
                    "status": "succeeded",
                    "amount": {
                        "value": "100.00",
                        "currency": "RUB"
                    }
                }
            }
            """;
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/webhooks/yookassa",
            webhookPayload,
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```


## 📊 Мониторинг и метрики

### Логирование

Библиотека использует SLF4J для логирования. Рекомендуемая конфигурация:

```yaml
logging:
  level:
    com.yookassa.spring: DEBUG
    com.yookassa.spring.client: INFO
    com.yookassa.spring.webhook: INFO
    com.yookassa.spring.scheduler: WARN
```


### Метрики (опционально)

Для интеграции с Micrometer добавьте:

```java
@Component
public class PaymentMetrics {
    
    private final Counter paymentCreatedCounter;
    private final Counter paymentSucceededCounter;
    private final Timer paymentProcessingTimer;
    
    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.paymentCreatedCounter = Counter.builder("yookassa.payments.created")
            .description("Number of created payments")
            .register(meterRegistry);
            
        this.paymentSucceededCounter = Counter.builder("yookassa.payments.succeeded")
            .description("Number of succeeded payments")
            .register(meterRegistry);
            
        this.paymentProcessingTimer = Timer.builder("yookassa.payments.processing.time")
            .description("Payment processing time")
            .register(meterRegistry);
    }
    
    @EventListener
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        paymentSucceededCounter.increment();
    }
}
```


## 🚨 Troubleshooting

### Частые проблемы

#### 1. Ошибка "No qualifying bean of type 'ObjectMapper'"

**Решение**: Убедитесь, что в classpath есть `jackson-databind`:

```gradle
implementation 'com.fasterxml.jackson.core:jackson-databind'
```


#### 2. Webhook не обрабатываются

**Проверьте**:

- Правильность URL webhook в личном кабинете ЮKassa
- Доступность эндпоинта извне (не localhost)
- Настройки firewall и load balancer


#### 3. Scheduler не работает

**Проверьте**:

- Включен ли scheduler в конфигурации
- Есть ли аннотация `@EnableScheduling` в конфигурации


### Отладка

Включите debug-логирование:

```yaml
logging:
  level:
    com.yookassa.spring: DEBUG
```


## 🤝 Участие в разработке

1. Fork репозитория
2. Создайте feature branch (`git checkout -b feature/amazing-feature`)
3. Commit изменения (`git commit -m 'Add amazing feature'`)
4. Push в branch (`git push origin feature/amazing-feature`)
5. Создайте Pull Request

### Требования для разработки

- Java 17+
- Gradle 7.0+
- Spring Boot 3.2+


### Запуск тестов

```bash
./gradlew test
```


### Сборка

```bash
./gradlew build
```


## 📄 Лицензия

Этот проект лицензирован под Apache License 2.0 - см. файл [LICENSE](LICENSE) для деталей.

## 🔗 Полезные ссылки

- [Официальная документация ЮKassa API](https://yookassa.ru/developers/api)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Issues и Feature Requests](https://github.com/your-org/yookassa-spring-boot-starter/issues)


## 📞 Поддержка

Если у вас есть вопросы или проблемы:

1. Проверьте [FAQ](#-troubleshooting)
2. Поищите в [Issues](https://github.com/your-org/yookassa-spring-boot-starter/issues)
3. Создайте новый [Issue](https://github.com/your-org/yookassa-spring-boot-starter/issues/new)

---

**Сделано с ❤️ для Spring Boot сообщества**

<div style="text-align: center">⁂</div>

[^1]: https://github.com/aaukhatov/spring-boot-rest/blob/master/README.md

[^2]: https://github.com/hmcts/spring-boot-template/blob/master/README.md

[^3]: https://git.miem.hse.ru/rstischenko/auto3dtrackingsystem/-/blob/master/README.md

[^4]: https://git.openforce.com/kevin.grote/java-spring-boot-example/-/blob/main/README.md

[^5]: https://habr.com/ru/articles/649363/

[^6]: https://javarush.com/groups/posts/3424-pishem-rezjume-na-github

[^7]: https://www.youtube.com/watch?v=FFBTGdEMrQ4

[^8]: https://opensource.tbank.ru/top-core-libs/warmup/-/blob/1.0.0/readme.md

