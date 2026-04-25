package com.bino.payment.notifier.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.bino.payment.notifier.exception.StripeSignatureException;
import com.bino.payment.notifier.service.NotificationService;
import com.bino.payment.notifier.stripe.StripeWebhookValidator;
import com.stripe.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class StripeWebhookHandlerTest {

    private StripeWebhookValidator validator;
    private NotificationService service;
    private StripeWebhookHandler handler;

    @BeforeEach
    void setUp() {
        validator = mock(StripeWebhookValidator.class);
        service = mock(NotificationService.class);
        handler = new StripeWebhookHandler(validator, service);
    }

    @Test
    void handleRequest_returnsNoFailuresOnSuccess() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn("evt_1");
        when(event.getType()).thenReturn("payment_intent.succeeded");
        when(validator.validate(anyString(), anyString())).thenReturn(event);

        SQSBatchResponse response = handler.handleRequest(sqsEvent("m1", "{}", "sig1"), mockContext());

        assertThat(response.getBatchItemFailures()).isEmpty();
        verify(service).process(event);
    }

    @Test
    void handleRequest_reportsFailureWhenServiceThrows() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn("evt_2");
        when(event.getType()).thenReturn("payment_intent.succeeded");
        when(validator.validate(anyString(), anyString())).thenReturn(event);
        doThrow(new RuntimeException("db down")).when(service).process(event);

        SQSBatchResponse response = handler.handleRequest(sqsEvent("m2", "{}", "sig2"), mockContext());

        assertThat(response.getBatchItemFailures())
                .singleElement()
                .extracting(SQSBatchResponse.BatchItemFailure::getItemIdentifier)
                .isEqualTo("m2");
    }

    @Test
    void handleRequest_dropsPoisonedMessageWithoutRetry() {
        when(validator.validate(anyString(), any()))
                .thenThrow(new StripeSignatureException("bad sig"));

        SQSBatchResponse response = handler.handleRequest(sqsEvent("m3", "{}", "bad"), mockContext());

        assertThat(response.getBatchItemFailures()).isEmpty();
        verify(service, never()).process(any());
    }

    private static SQSEvent sqsEvent(String messageId, String body, String signature) {
        SQSEvent.SQSMessage m = new SQSEvent.SQSMessage();
        m.setMessageId(messageId);
        m.setBody(body);
        SQSEvent.MessageAttribute attr = new SQSEvent.MessageAttribute();
        attr.setStringValue(signature);
        attr.setDataType("String");
        m.setMessageAttributes(Map.of("Stripe-Signature", attr));

        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(m));
        return event;
    }

    private static Context mockContext() {
        Context ctx = mock(Context.class);
        when(ctx.getAwsRequestId()).thenReturn("req-1");
        return ctx;
    }
}
