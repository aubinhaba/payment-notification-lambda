package com.bino.payment.notifier.stripe;

import com.bino.payment.notifier.domain.NotificationStatus;
import com.bino.payment.notifier.domain.PaymentNotification;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;

/**
 * Maps a verified {@code payment_intent.succeeded} event to a {@link PaymentNotification}
 * in {@code PENDING} state. Fails loudly if any required field is missing — by contract
 * Stripe guarantees these on a successful PaymentIntent.
 */
public final class PaymentIntentExtractor {

    public static final String EVENT_TYPE = "payment_intent.succeeded";

    private PaymentIntentExtractor() {}

    public static PaymentNotification toPendingNotification(Event event) {
        StripeObject obj = deserialize(event);

        if (!(obj instanceof PaymentIntent pi)) {
            throw new IllegalArgumentException(
                    "Expected PaymentIntent payload for eventId=" + event.getId()
                            + " but got " + obj.getClass().getSimpleName());
        }

        String email = resolveEmail(pi);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "PaymentIntent " + pi.getId() + " has no customer email (receipt_email / metadata.customer_email)");
        }
        if (pi.getCurrency() == null || pi.getAmount() == null) {
            throw new IllegalArgumentException("PaymentIntent " + pi.getId() + " missing amount/currency");
        }

        return PaymentNotification.builder()
                .stripeEventId(event.getId())
                .stripePaymentIntentId(pi.getId())
                .amount(pi.getAmount())
                .currency(pi.getCurrency().toUpperCase())
                .customerEmail(email)
                .notificationStatus(NotificationStatus.PENDING)
                .retryCount(0)
                .build();
    }

    // getObject() returns empty when the event's Stripe API version differs from the SDK's
    // compiled version — deserializeUnsafe() skips the version check.
    private static StripeObject deserialize(Event event) {
        EventDataObjectDeserializer d = event.getDataObjectDeserializer();
        if (d.getObject().isPresent()) {
            return d.getObject().get();
        }
        try {
            return d.deserializeUnsafe();
        } catch (EventDataObjectDeserializationException e) {
            throw new IllegalArgumentException(
                    "Cannot deserialise Stripe event data for eventId=" + event.getId(), e);
        }
    }

    private static String resolveEmail(PaymentIntent pi) {
        if (pi.getReceiptEmail() != null && !pi.getReceiptEmail().isBlank()) {
            return pi.getReceiptEmail();
        }
        if (pi.getMetadata() != null) {
            return pi.getMetadata().get("customer_email");
        }
        return null;
    }
}
