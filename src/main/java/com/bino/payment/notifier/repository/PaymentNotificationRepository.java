package com.bino.payment.notifier.repository;

import com.bino.payment.notifier.domain.PaymentNotification;

import java.util.Optional;

public interface PaymentNotificationRepository {

    Optional<PaymentNotification> findByStripeEventId(String stripeEventId);

    PaymentNotification insert(PaymentNotification entity);

    PaymentNotification update(PaymentNotification entity);
}
