package com.bino.payment.notifier.channel;

import com.bino.payment.notifier.domain.PaymentNotification;
import com.bino.payment.notifier.exception.NotificationDeliveryException;

/**
 * Transport that delivers a customer-facing notification.
 */
public interface NotificationChannel {

    /** Stable identifier used in logs (e.g. {@code "EMAIL_SES"}, {@code "SMS_SNS"}). */
    String id();

    /**
     * @throws NotificationDeliveryException when the delivery attempt fails and should be retried.
     */
    void send(PaymentNotification notification);
}
