package com.yookassa.spring.scheduler;

import com.yookassa.spring.domain.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service  // Добавить эту аннотацию
public class PaymentStatusService {

    private final PaymentStatusRepository paymentStatusRepository;

    public PaymentStatusService(PaymentStatusRepository paymentStatusRepository) {
        this.paymentStatusRepository = paymentStatusRepository;
    }

    public List<String> getPendingPaymentIds() {
        return paymentStatusRepository.findPendingPaymentIds();
    }

    public PaymentStatus getLastKnownStatus(String paymentId) {
        return paymentStatusRepository.findStatusByPaymentId(paymentId);
    }

    public void updatePaymentStatus(String paymentId, PaymentStatus status) {
        paymentStatusRepository.updatePaymentStatus(paymentId, status);
    }

    public void trackPayment(String paymentId, PaymentStatus initialStatus) {
        paymentStatusRepository.savePaymentStatus(paymentId, initialStatus);
    }

    public void stopTracking(String paymentId) {
        paymentStatusRepository.removePaymentTracking(paymentId);
    }
}