package com.bino.payment.notifier.channel;

import com.bino.payment.notifier.domain.PaymentNotification;
import com.bino.payment.notifier.exception.NotificationDeliveryException;

/**
 * Transport that delivers a customer-facing notification.
 * Implementations are stateless and thread-safe.
 *
 * <p>The service layer persists the {@link PaymentNotification} before calling {@link #send};
 * channels must not write to the database or mutate the entity.
 */
public interface NotificationChannel {

    /** Stable identifier used in logs (e.g. {@code "EMAIL_SES"}, {@code "SMS_SNS"}). */
    String id();

    /**
     * @throws NotificationDeliveryException when the delivery attempt fails and should be retried.
     */
    void send(PaymentNotification notification);
}
