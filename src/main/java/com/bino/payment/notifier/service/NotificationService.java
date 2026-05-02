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

        log.info("Received Stripe event id={} type={} apiVersion={} created={} livemode={} pendingWebhooks={} requestId={}",
                eventId, event.getType(), event.getApiVersion(),
                event.getCreated(), event.getLivemode(), event.getPendingWebhooks(),
                event.getRequest() != null ? event.getRequest().getId() : null);
        if (log.isDebugEnabled()) {
            log.debug("Full Stripe event payload: {}", event.toJson());
        }

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
        log.info("Extracted notification paymentIntentId={} amount={} currency={} email={}",
                notification.getStripePaymentIntentId(), notification.getAmount(),
                notification.getCurrency(), notification.getCustomerEmail());

        repository.insert(notification);
        log.info("Notification persisted id={} eventId={}", notification.getId(), eventId);

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
