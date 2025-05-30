package com.yookassa.spring.scheduler;

import com.yookassa.spring.domain.PaymentStatus;

import java.util.List;

public interface PaymentStatusRepository {
    List<String> findPendingPaymentIds();
    PaymentStatus findStatusByPaymentId(String paymentId);
    void updatePaymentStatus(String paymentId, PaymentStatus status);
    void savePaymentStatus(String paymentId, PaymentStatus status);
    void removePaymentTracking(String paymentId);
}