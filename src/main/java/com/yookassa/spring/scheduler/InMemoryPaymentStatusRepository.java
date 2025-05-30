package com.yookassa.spring.scheduler;

import com.yookassa.spring.domain.PaymentStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnMissingBean(PaymentStatusRepository.class)
public class InMemoryPaymentStatusRepository implements PaymentStatusRepository {

    private final Map<String, PaymentStatus> paymentStatuses = new ConcurrentHashMap<>();

    @Override
    public List<String> findPendingPaymentIds() {
        return paymentStatuses.entrySet().stream()
                .filter(entry -> entry.getValue() == PaymentStatus.PENDING ||
                        entry.getValue() == PaymentStatus.WAITING_FOR_CAPTURE)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public PaymentStatus findStatusByPaymentId(String paymentId) {
        return paymentStatuses.get(paymentId);
    }

    @Override
    public void updatePaymentStatus(String paymentId, PaymentStatus status) {
        paymentStatuses.put(paymentId, status);

        // Удаляем завершенные платежи из отслеживания
        if (status == PaymentStatus.SUCCEEDED || status == PaymentStatus.CANCELED) {
            paymentStatuses.remove(paymentId);
        }
    }

    @Override
    public void savePaymentStatus(String paymentId, PaymentStatus status) {
        paymentStatuses.put(paymentId, status);
    }

    @Override
    public void removePaymentTracking(String paymentId) {
        paymentStatuses.remove(paymentId);
    }
}