

# YooKassa Spring Boot Starter

Библиотека для интеграции с платежной системой ЮКасса в Spring Boot приложениях.

## Возможности

- 🚀 Автоматическая конфигурация Spring Boot
- 💳 Полная поддержка YooKassa API v3
- 📡 Обработка webhook уведомлений
- ⏰ Автоматический мониторинг статусов платежей
- 🔒 Валидация IP адресов и подписей webhook
- 📊 Event-driven архитектура для обработки событий
- 🛡️ Безопасное логирование с маскированием чувствительных данных




## Установка

### Gradle

Добавьте JitPack репозиторий в ваш `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Затем добавьте зависимость в `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.GridyushkoF:YooKassaJavaSdk:v1.0.6'
}
```


### Maven

Добавьте JitPack репозиторий в ваш `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Затем добавьте зависимость:

```xml
<dependency>
    <groupId>com.github.GridyushkoF</groupId>
    <artifactId>YooKassaJavaSdk</artifactId>
    <version>v1.0.6</version>
</dependency>
```

### Альтернативные версии

Вы также можете использовать:

- Последний коммит из main ветки:
- Конкретный коммит: `commit-hash`
- Другой тег: , например `v1.0.5`
- Но рекоммендуется использовать последнюю версию чтобы не упускать новые возможности


## Быстрый старт

### 1. Настройка конфигурации

Добавьте в `application.properties`:

```properties
# Обязательные параметры
yookassa.shop-id=your-shop-id
yookassa.secret-key=your-secret-key

# Опциональные параметры
yookassa.api-url=https://api.yookassa.ru
yookassa.webhook.path=/api/webhooks/yookassa
yookassa.webhook.signature-validation-enabled=true
yookassa.scheduler.enabled=true
yookassa.scheduler.check-interval=60000
```


### 2. Создание платежа

```java
@RestController
@RequiredArgsConstructor
public class PaymentController {
    
    private final YooKassaService yooKassaService;
    
    @PostMapping("/create-payment")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = yooKassaService.createPayment(request);
        return ResponseEntity.ok(response);
    }
}
```


### 3. Обработка событий платежей

```java
@Configuration
public class PaymentConfiguration {
    
    @Autowired
    private PaymentHandlerRegistry handlerRegistry;
    
    @PostConstruct
    public void setupPaymentHandlers() {
        handlerRegistry
            .onPaymentSucceeded(event -> {
                Payment payment = event.getPayment();
                log.info("Платеж успешен: {}, сумма: {}", 
                    payment.getId(), payment.getAmount().getValue());
                // Ваша бизнес-логика
            })
            .onPaymentCanceled(event -> {
                Payment payment = event.getPayment();
                log.warn("Платеж отменен: {}", payment.getId());
                // Обработка отмены
            })
            .onPaymentWaitingForCapture(event -> {
                Payment payment = event.getPayment();
                log.info("Платеж ожидает подтверждения: {}", payment.getId());
                // Резервирование товара
            });
    }
}
```


## Детальное использование

### Создание платежа

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final YooKassaService yooKassaService;
    
    public String createPaymentForOrder(Order order) {
        PaymentRequest request = PaymentRequest.builder()
            .amount(order.getTotalAmount())
            .currency("RUB")
            .description("Оплата заказа #" + order.getId())
            .returnUrl("https://myshop.com/success")
            .capture(true)
            .metadata(Metadata.builder()
                .orderId(order.getId().toString())
                .userId(order.getUserId().toString())
                .build())
            .build();
            
        PaymentResponse response = yooKassaService.createPayment(request);
        return response.getConfirmationUrl();
    }
}
```


### Получение информации о платеже

```java
@GetMapping("/payment/{paymentId}")
public ResponseEntity<Payment> getPayment(@PathVariable String paymentId) {
    Payment payment = yooKassaService.getPaymentInfo(paymentId);
    return ResponseEntity.ok(payment);
}
```


### Подтверждение платежа

```java
@PostMapping("/capture/{paymentId}")
public ResponseEntity<Payment> capturePayment(
    @PathVariable String paymentId,
    @RequestParam BigDecimal amount) {
    
    Payment payment = yooKassaService.capturePayment(paymentId, amount);
    return ResponseEntity.ok(payment);
}
```


### Отмена платежа

```java
@PostMapping("/cancel/{paymentId}")
public ResponseEntity<Payment> cancelPayment(@PathVariable String paymentId) {
    Payment payment = yooKassaService.cancelPayment(paymentId);
    return ResponseEntity.ok(payment);
}
```


## Обработка webhook уведомлений

Библиотека автоматически настраивает endpoint для получения webhook уведомлений от YooKassa. По умолчанию endpoint доступен по адресу `/api/webhooks/yookassa`.

### Настройка webhook в YooKassa

1. Войдите в личный кабинет YooKassa
2. Перейдите в раздел "Настройки" → "Уведомления"
3. Укажите URL: `https://your-domain.com/api/webhooks/yookassa`
4. Выберите события для уведомлений

### Валидация webhook

```properties
# Включить валидацию подписи webhook
yookassa.webhook.signature-validation-enabled=true
yookassa.webhook.secret-key=your-webhook-secret-key
```


## Автоматический мониторинг платежей

Библиотека включает scheduler для автоматической проверки статусов платежей:

```properties
# Включить мониторинг
yookassa.scheduler.enabled=true
# Интервал проверки в миллисекундах (по умолчанию 60 секунд)
yookassa.scheduler.check-interval=60000
# Задержка между запросами в миллисекундах
yookassa.scheduler.delay-between-requests=1000
```


## События платежей

Библиотека генерирует следующие события:

- `PaymentSucceededEvent` - платеж успешно завершен
- `PaymentCanceledEvent` - платеж отменен
- `PaymentWaitingForCaptureEvent` - платеж ожидает подтверждения
- `PaymentStatusChangedEvent` - изменился статус платежа
- `PaymentExpiredEvent` - платеж истек
- `RefundSucceededEvent` - возврат успешно выполнен


### Подписка на события через @EventListener

```java
@Component
@Slf4j
public class PaymentEventListener {

    @EventListener
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        Payment payment = event.getPayment();
        log.info("Платеж {} успешно завершен", payment.getId());
        // Обновление статуса заказа
        // Отправка уведомления клиенту
    }

    @EventListener
    public void handlePaymentCanceled(PaymentCanceledEvent event) {
        Payment payment = event.getPayment();
        String reason = event.getReason();
        log.warn("Платеж {} отменен. Причина: {}", payment.getId(), reason);
        // Возврат товара в наличие
        // Уведомление о неудачной оплате
    }
}
```


## Обработка ошибок

Библиотека предоставляет специализированные исключения:

- `YooKassaApiException` - ошибки API YooKassa
- `PaymentProcessingException` - ошибки обработки платежей
- `YooKassaServiceException` - общие ошибки сервиса


### Глобальная обработка ошибок

```java
@ControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler(YooKassaApiException.class)
    public ResponseEntity<ErrorResponse> handleYooKassaApiException(YooKassaApiException e) {
        ErrorResponse error = ErrorResponse.builder()
            .error("PAYMENT_API_ERROR")
            .message(e.getMessage())
            .statusCode(e.getStatusCode())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(e.getStatusCode()).body(error);
    }
}
```


## Валидация данных

Библиотека включает встроенную валидацию:

```java
@Service
public class PaymentService {
    
    @Autowired
    private PaymentValidator validator;
    
    public PaymentResponse createPayment(PaymentRequest request) {
        // Автоматическая валидация
        validator.validatePaymentRequest(request);
        
        // Создание платежа
        return yooKassaService.createPayment(request);
    }
}
```


## Конфигурация

### Полный список параметров

```properties
# Основные настройки
yookassa.shop-id=your-shop-id
yookassa.secret-key=your-secret-key
yookassa.api-url=https://api.yookassa.ru

# Webhook настройки
yookassa.webhook.path=/api/webhooks/yookassa
yookassa.webhook.signature-validation-enabled=true
yookassa.webhook.secret-key=your-webhook-secret

# Scheduler настройки
yookassa.scheduler.enabled=true
yookassa.scheduler.check-interval=60000
yookassa.scheduler.delay-between-requests=1000
```


### Кастомизация компонентов

```java
@Configuration
public class CustomYooKassaConfig {

    @Bean
    @Primary
    public PaymentStatusRepository customPaymentStatusRepository() {
        // Ваша реализация хранилища статусов
        return new DatabasePaymentStatusRepository();
    }

    @Bean
    @Primary
    public PaymentValidator customPaymentValidator() {
        // Кастомная валидация
        return new EnhancedPaymentValidator();
    }
}
```


## Примеры использования

### Интеграция с заказами

```java
@Service
@Transactional
public class OrderPaymentService {

    @Autowired
    private YooKassaService yooKassaService;
    
    @Autowired
    private OrderRepository orderRepository;

    public String processOrderPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        PaymentRequest request = PaymentRequest.builder()
            .amount(order.getTotalAmount())
            .currency("RUB")
            .description("Оплата заказа #" + order.getNumber())
            .returnUrl("https://shop.com/orders/" + orderId + "/success")
            .capture(false) // Двухстадийный платеж
            .metadata(Metadata.builder()
                .orderId(orderId.toString())
                .customerEmail(order.getCustomerEmail())
                .build())
            .build();

        PaymentResponse response = yooKassaService.createPayment(request);
        
        order.setPaymentId(response.getPaymentId());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        orderRepository.save(order);

        return response.getConfirmationUrl();
    }
}
```


### Обработка успешного платежа

```java
@Component
public class OrderPaymentHandler {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PaymentHandlerRegistry handlerRegistry;

    @PostConstruct
    public void setupHandlers() {
        handlerRegistry
            .onPaymentSucceeded(this::handleSuccessfulPayment)
            .onPaymentCanceled(this::handleCanceledPayment);
    }

    private void handleSuccessfulPayment(PaymentSucceededEvent event) {
        Payment payment = event.getPayment();
        String orderId = payment.getMetadata().getOrderId();
        
        if (orderId != null) {
            orderService.markAsPaid(Long.valueOf(orderId));
            notificationService.sendPaymentConfirmation(orderId);
        }
    }

    private void handleCanceledPayment(PaymentCanceledEvent event) {
        Payment payment = event.getPayment();
        String orderId = payment.getMetadata().getOrderId();
        
        if (orderId != null) {
            orderService.markAsPaymentFailed(Long.valueOf(orderId));
            notificationService.sendPaymentFailureNotification(orderId);
        }
    }
}
```


## Тестирование

### Тестовые настройки

```properties
# application-test.properties
yookassa.shop-id=test-shop-id
yookassa.secret-key=test-secret-key
yookassa.api-url=https://api.yookassa.ru
yookassa.scheduler.enabled=false
```


### Пример теста

```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class PaymentIntegrationTest {

    @Autowired
    private YooKassaService yooKassaService;

    @Test
    void shouldCreatePayment() {
        PaymentRequest request = PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .currency("RUB")
            .description("Test payment")
            .returnUrl("https://example.com/success")
            .capture(true)
            .build();

        PaymentResponse response = yooKassaService.createPayment(request);

        assertThat(response.getPaymentId()).isNotNull();
        assertThat(response.getConfirmationUrl()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }
}
```


## Логирование

Библиотека поддерживает детальное логирование всех операций:

```properties
# Включить DEBUG логирование
logging.level.com.yookassa.spring=DEBUG

# Логирование HTTP запросов
logging.level.org.apache.http.wire=DEBUG
```


## Безопасность

### Рекомендации по безопасности

1. **Никогда не храните секретные ключи в коде**
2. **Используйте переменные окружения для production**
3. **Включите валидацию webhook подписей**
4. **Ограничьте доступ к webhook endpoint по IP**

### Настройка через переменные окружения

```bash
export YOOKASSA_SHOP_ID=your-shop-id
export YOOKASSA_SECRET_KEY=your-secret-key
export YOOKASSA_WEBHOOK_SECRET=your-webhook-secret
```

```properties
yookassa.shop-id=${YOOKASSA_SHOP_ID}
yookassa.secret-key=${YOOKASSA_SECRET_KEY}
yookassa.webhook.secret-key=${YOOKASSA_WEBHOOK_SECRET}
```


## Поддержка и вклад в развитие

- **GitHub**: текущий
- **Issues**: Сообщайте о проблемах через GitHub Issues
- **Pull Requests**: Приветствуются предложения по улучшению
- **Перспективы**: Проект позиционирует себя как независимый неофициальный SDK, но самый лучший из тех что есть, с гибким функционалом и качественной документацией. Я буду рад вашей обратной связи и новым предложениям


## Лицензия

Этот проект распространяется под лицензией MIT. См. файл `LICENSE` для подробностей.

## Changelog

### v1.0.6

- Исправлена автоконфигурация Spring Boot
- Добавлена поддержка изолированного ObjectMapper
- Улучшено логирование HTTP запросов
- Исправлены проблемы с snake_case форматированием


### v1.0.5

- Добавлена поддержка webhook валидации
- Улучшена обработка ошибок
- Добавлены новые события платежей


### v1.0.0

- Первый релиз
- Базовая функциональность YooKassa API
- Автоконфигурация Spring Boot
- Event-driven архитектура



