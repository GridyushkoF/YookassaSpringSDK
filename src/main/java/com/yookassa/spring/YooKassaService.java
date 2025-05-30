package com.yookassa.spring;

import com.yookassa.spring.client.CapturePaymentRequest;
import com.yookassa.spring.client.CreatePaymentRequest;
import com.yookassa.spring.client.YooKassaClient;
import com.yookassa.spring.domain.Amount;
import com.yookassa.spring.domain.Confirmation;
import com.yookassa.spring.domain.Payment;
import com.yookassa.spring.events.PaymentCanceledEvent;
import com.yookassa.spring.events.PaymentSucceededEvent;
import com.yookassa.spring.exception.YooKassaServiceException;
import com.yookassa.spring.scheduler.PaymentStatusService;
import com.yookassa.spring.validation.PaymentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class YooKassaService {

    private final YooKassaClient client;
    private final PaymentStatusService paymentStatusService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentValidator paymentValidator;


    public PaymentResponse createPayment(PaymentRequest request) {
        paymentValidator.validatePaymentRequest(request);
        try {
            CreatePaymentRequest apiRequest = CreatePaymentRequest.builder()
                    .amount(Amount.builder()
                            .value(request.getAmount())
                            .currency(request.getCurrency())
                            .build())
                    .description(request.getDescription())
                    .confirmation(Confirmation.builder()
                            .type("redirect")
                            .returnUrl(request.getReturnUrl())
                            .build())
                    .capture(request.isCapture())
                    .metadata(request.getMetadata())
                    .build();

            Payment payment = client.createPayment(apiRequest);

            // Начинаем отслеживание статуса платежа
            paymentStatusService.trackPayment(payment.getId(), payment.getStatus());

            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .status(payment.getStatus())
                    .confirmationUrl(payment.getConfirmation().getConfirmationUrl())
                    .amount(payment.getAmount())
                    .build();

        } catch (Exception e) {
            log.error("Failed to create payment", e);
            throw new YooKassaServiceException("Failed to create payment", e);
        }
    }

    public String generatePaymentUrl(PaymentRequest request) {
        PaymentResponse response = createPayment(request);
        return response.getConfirmationUrl();
    }

    public Payment getPaymentInfo(String paymentId) {
        return client.getPayment(paymentId);
    }

    public Payment capturePayment(String paymentId, BigDecimal amount) {
        CapturePaymentRequest request = CapturePaymentRequest.builder()
                .amount(Amount.builder()
                        .value(amount)
                        .currency("RUB")
                        .build())
                .build();

        Payment payment = client.capturePayment(paymentId, request);

        // Публикуем событие о захвате платежа
        eventPublisher.publishEvent(new PaymentSucceededEvent(payment));

        return payment;
    }

    public Payment cancelPayment(String paymentId) {
        Payment payment = client.cancelPayment(paymentId);

        // Прекращаем отслеживание отмененного платежа
        paymentStatusService.stopTracking(paymentId);

        // Публикуем событие об отмене платежа
        eventPublisher.publishEvent(new PaymentCanceledEvent(payment, "manual_cancellation"));

        return payment;
    }
}
