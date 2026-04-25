package com.bino.payment.notifier.service;

import com.bino.payment.notifier.channel.NotificationChannel;
import com.bino.payment.notifier.domain.NotificationStatus;
import com.bino.payment.notifier.domain.PaymentNotification;
import com.bino.payment.notifier.exception.NotificationDeliveryException;
import com.bino.payment.notifier.repository.PaymentNotificationRepository;
import com.bino.payment.notifier.stripe.PaymentIntentExtractor;
import com.stripe.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Orchestrates the processing of a verified Stripe event:
 * <ol>
 *   <li>skip silently when {@code stripe_event_id} already exists (strict idempotency)</li>
 *   <li>skip when the event type is not {@code payment_intent.succeeded}</li>
 *   <li>persist a {@link NotificationStatus#PENDING} record</li>
 *   <li>deliver the notification via the configured {@link NotificationChannel}</li>
 *   <li>transition to {@code SENT} on success, {@code FAILED} + retry marker on delivery error</li>
 * </ol>
 */
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final PaymentNotificationRepository repository;
    private final NotificationChannel channel;

    public NotificationService(PaymentNotificationRepository repository, NotificationChannel channel) {
        this.repository = repository;
        this.channel = channel;
    }

    public void process(Event event) {
        String eventId = event.getId();

        if (!PaymentIntentExtractor.EVENT_TYPE.equals(event.getType())) {
            log.info("Skipping unsupported Stripe event type={} id={}", event.getType(), eventId);
            return;
        }

        Optional<PaymentNotification> existing = repository.findByStripeEventId(eventId);
        if (existing.isPresent()) {
            log.info("Event already processed, skipping id={}", eventId);
            return;
        }

        PaymentNotification notification = PaymentIntentExtractor.toPendingNotification(event);
        repository.insert(notification);

        try {
            channel.send(notification);
            notification.setNotificationStatus(NotificationStatus.SENT);
            repository.update(notification);
            log.info("Notification sent channel={} eventId={}", channel.id(), eventId);
        } catch (NotificationDeliveryException e) {
            notification.setNotificationStatus(NotificationStatus.FAILED);
            notification.setRetryCount(notification.getRetryCount() + 1);
            repository.update(notification);
            log.warn("Notification delivery failed, rethrowing for SQS retry eventId={}", eventId, e);
            throw e;
        }
    }
}
