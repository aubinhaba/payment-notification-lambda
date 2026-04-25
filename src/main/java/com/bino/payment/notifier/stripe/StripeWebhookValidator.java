package com.bino.payment.notifier.stripe;

import com.bino.payment.notifier.exception.StripeSignatureException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

/**
 * Verifies the {@code Stripe-Signature} header against the raw webhook payload and the
 * endpoint signing secret, and returns the parsed Stripe {@link Event}.
 *
 * <p>This is the first call made by the Lambda handler — any invalid signature short-circuits
 * processing and the message is routed to the DLQ.
 */
public class StripeWebhookValidator {

    private static final long DEFAULT_TOLERANCE_SECONDS = 300L;

    private final String webhookSecret;
    private final long toleranceSeconds;

    public StripeWebhookValidator(String webhookSecret) {
        this(webhookSecret, DEFAULT_TOLERANCE_SECONDS);
    }

    public StripeWebhookValidator(String webhookSecret, long toleranceSeconds) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalArgumentException("Stripe webhook secret must be provided");
        }
        this.webhookSecret = webhookSecret;
        this.toleranceSeconds = toleranceSeconds;
    }

    public Event validate(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new StripeSignatureException("Missing Stripe-Signature header");
        }
        try {
            return Webhook.constructEvent(payload, signatureHeader, webhookSecret, toleranceSeconds);
        } catch (SignatureVerificationException e) {
            throw new StripeSignatureException("Invalid Stripe signature", e);
        }
    }
}
