package com.bino.payment.notifier.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.bino.payment.notifier.config.AppConfig;
import com.bino.payment.notifier.exception.StripeSignatureException;
import com.bino.payment.notifier.service.NotificationService;
import com.bino.payment.notifier.stripe.StripeWebhookValidator;
import com.stripe.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point invoked by AWS Lambda when Stripe webhook events arrive from SQS.
 */
public class StripeWebhookHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookHandler.class);
    private static final String SIGNATURE_HEADER_ATTRIBUTE = "Stripe-Signature";

    private final StripeWebhookValidator validator;
    private final NotificationService notificationService;

    public StripeWebhookHandler() {
        AppConfig config = AppConfig.getInstance();
        this.validator = config.getStripeWebhookValidator();
        this.notificationService = config.getNotificationService();
    }

    StripeWebhookHandler(StripeWebhookValidator validator, NotificationService notificationService) {
        this.validator = validator;
        this.notificationService = notificationService;
    }

    @Override
    public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
        String requestId = context != null ? context.getAwsRequestId() : "unknown";
        int size = sqsEvent.getRecords() != null ? sqsEvent.getRecords().size() : 0;
        log.info("Lambda invocation start requestId={} recordCount={}", requestId, size);

        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();

        for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
            String messageId = message.getMessageId();
            try {
                Event event = validator.validate(message.getBody(), signatureHeaderOf(message));
                notificationService.process(event);
            } catch (StripeSignatureException e) {
                log.error("Dropping message with invalid Stripe signature messageId={}", messageId, e);
            } catch (RuntimeException e) {
                log.error("Processing failed, scheduling SQS retry messageId={}", messageId, e);
                failures.add(SQSBatchResponse.BatchItemFailure.builder()
                        .withItemIdentifier(messageId)
                        .build());
            }
        }

        log.info("Lambda invocation end requestId={} failedCount={}", requestId, failures.size());
        return SQSBatchResponse.builder().withBatchItemFailures(failures).build();
    }

    private static String signatureHeaderOf(SQSEvent.SQSMessage message) {
        if (message.getMessageAttributes() == null) return null;
        SQSEvent.MessageAttribute attr = message.getMessageAttributes().get(SIGNATURE_HEADER_ATTRIBUTE);
        return attr == null ? null : attr.getStringValue();
    }
}
