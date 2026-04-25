package com.bino.payment.notifier.channel;

import com.bino.payment.notifier.domain.NotificationStatus;
import com.bino.payment.notifier.domain.PaymentNotification;
import com.bino.payment.notifier.exception.NotificationDeliveryException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SesEmailChannelTest {

    @Test
    void send_buildsEmailAndCallsSes() {
        SesV2Client ses = mock(SesV2Client.class);
        when(ses.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("ses-1").build());

        SesEmailChannel channel = new SesEmailChannel(ses, "from@example.com", "cfg");
        channel.send(sampleNotification());

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(ses).sendEmail(captor.capture());
        SendEmailRequest sent = captor.getValue();
        assertThat(sent.fromEmailAddress()).isEqualTo("from@example.com");
        assertThat(sent.destination().toAddresses()).containsExactly("to@example.com");
        assertThat(sent.content().simple().subject().data()).contains("Payment confirmation");
        assertThat(sent.configurationSetName()).isEqualTo("cfg");
    }

    @Test
    void send_throwsNotificationDeliveryExceptionOnSesError() {
        SesV2Client ses = mock(SesV2Client.class);
        SesV2Exception exception = (SesV2Exception) SesV2Exception.builder()
                .message("throttled")
                .awsErrorDetails(AwsErrorDetails.builder().errorCode("Throttling").build())
                .build();
        when(ses.sendEmail(any(SendEmailRequest.class))).thenThrow(exception);

        SesEmailChannel channel = new SesEmailChannel(ses, "from@example.com", null);

        assertThatThrownBy(() -> channel.send(sampleNotification()))
                .isInstanceOf(NotificationDeliveryException.class)
                .hasMessageContaining("Throttling");
    }

    @Test
    void id_isStable() {
        assertThat(new SesEmailChannel(mock(SesV2Client.class), "f@x", null).id())
                .isEqualTo("EMAIL_SES");
    }

    private static PaymentNotification sampleNotification() {
        return PaymentNotification.builder()
                .stripeEventId("evt_1")
                .stripePaymentIntentId("pi_1")
                .amount(1500L)
                .currency("EUR")
                .customerEmail("to@example.com")
                .notificationStatus(NotificationStatus.PENDING)
                .build();
    }
}
