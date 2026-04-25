package com.bino.payment.notifier.stripe;

import com.bino.payment.notifier.domain.NotificationStatus;
import com.bino.payment.notifier.domain.PaymentNotification;
import com.stripe.model.Event;
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
        StripeObject obj = event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot deserialise Stripe event data for eventId=" + event.getId()));

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
