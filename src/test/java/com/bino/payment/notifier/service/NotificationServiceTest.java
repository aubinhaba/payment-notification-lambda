package com.bino.payment.notifier.service;

import com.bino.payment.notifier.channel.NotificationChannel;
import com.bino.payment.notifier.domain.NotificationStatus;
import com.bino.payment.notifier.domain.PaymentNotification;
import com.bino.payment.notifier.exception.NotificationDeliveryException;
import com.bino.payment.notifier.repository.PaymentNotificationRepository;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private PaymentNotificationRepository repository;
    private NotificationChannel channel;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(PaymentNotificationRepository.class);
        channel = mock(NotificationChannel.class);
        when(channel.id()).thenReturn("EMAIL_SES");
        service = new NotificationService(repository, channel);
    }

    @Test
    void process_sendsEmailAndMarksSent() {
        Event event = buildEvent("evt_ok", "payment_intent.succeeded",
                paymentIntent("pi_1", "customer@example.com", 1500L, "eur"));
        when(repository.findByStripeEventId("evt_ok")).thenReturn(Optional.empty());

        service.process(event);

        verify(repository).insert(any(PaymentNotification.class));
        verify(channel).send(any(PaymentNotification.class));

        ArgumentCaptor<PaymentNotification> updateCaptor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(repository).update(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getNotificationStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void process_marksFailedAndRethrowsWhenDeliveryFails() {
        Event event = buildEvent("evt_fail", "payment_intent.succeeded",
                paymentIntent("pi_2", "bob@example.com", 999L, "eur"));
        when(repository.findByStripeEventId("evt_fail")).thenReturn(Optional.empty());
        doThrow(new NotificationDeliveryException("ses down"))
                .when(channel).send(any(PaymentNotification.class));

        assertThatThrownBy(() -> service.process(event))
                .isInstanceOf(NotificationDeliveryException.class);

        ArgumentCaptor<PaymentNotification> captor = ArgumentCaptor.forClass(PaymentNotification.class);
        verify(repository).update(captor.capture());
        assertThat(captor.getValue().getNotificationStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(captor.getValue().getRetryCount()).isEqualTo(1);
    }

    @Test
    void process_skipsSilentlyWhenAlreadyProcessed() {
        Event event = buildEvent("evt_dup", "payment_intent.succeeded",
                paymentIntent("pi_3", "c@example.com", 100L, "eur"));
        when(repository.findByStripeEventId("evt_dup"))
                .thenReturn(Optional.of(PaymentNotification.builder()
                        .stripeEventId("evt_dup")
                        .notificationStatus(NotificationStatus.SENT)
                        .build()));

        service.process(event);

        verify(channel, never()).send(any());
        verify(repository, never()).insert(any());
        verify(repository, never()).update(any());
    }

    @Test
    void process_skipsUnsupportedEventType() {
        Event event = buildEvent("evt_other", "customer.created", null);

        service.process(event);

        verify(repository, never()).findByStripeEventId(any());
        verify(channel, never()).send(any());
    }

    private static PaymentIntent paymentIntent(String id, String email, long amount, String currency) {
        PaymentIntent pi = new PaymentIntent();
        pi.setId(id);
        pi.setReceiptEmail(email);
        pi.setAmount(amount);
        pi.setCurrency(currency);
        return pi;
    }

    private static Event buildEvent(String id, String type, Object dataObject) {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(id);
        when(event.getType()).thenReturn(type);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.ofNullable((StripeObject) dataObject));
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        return event;
    }
}
