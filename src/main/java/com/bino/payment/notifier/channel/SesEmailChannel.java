package com.bino.payment.notifier.channel;

import com.bino.payment.notifier.domain.PaymentNotification;
import com.bino.payment.notifier.exception.NotificationDeliveryException;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

public class SesEmailChannel implements NotificationChannel {

    public static final String ID = "EMAIL_SES";

    private final SesV2Client sesClient;
    private final String fromAddress;
    private final String configurationSet;

    public SesEmailChannel(SesV2Client sesClient, String fromAddress, String configurationSet) {
        this.sesClient = sesClient;
        this.fromAddress = fromAddress;
        this.configurationSet = configurationSet;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void send(PaymentNotification notification) {
        String formattedAmount = formatAmount(notification.getAmount(), notification.getCurrency());
        String subject = "Payment confirmation — " + formattedAmount;
        String body = """
                Hello,

                We have received your payment of %s.

                Your transaction reference is %s.

                Thank you for your order.
                """.formatted(formattedAmount, notification.getStripePaymentIntentId());

        SendEmailRequest.Builder builder = SendEmailRequest.builder()
                .fromEmailAddress(fromAddress)
                .destination(Destination.builder().toAddresses(notification.getCustomerEmail()).build())
                .content(EmailContent.builder()
                        .simple(Message.builder()
                                .subject(Content.builder().data(subject).charset("UTF-8").build())
                                .body(Body.builder()
                                        .text(Content.builder().data(body).charset("UTF-8").build())
                                        .build())
                                .build())
                        .build());

        if (configurationSet != null && !configurationSet.isBlank()) {
            builder.configurationSetName(configurationSet);
        }

        try {
            sesClient.sendEmail(builder.build());
        } catch (SesV2Exception e) {
            throw new NotificationDeliveryException(
                    "SES send failed: " + e.awsErrorDetails().errorCode() + " — " + e.getMessage(), e);
        }
    }

    private static String formatAmount(long amountMinor, String currency) {
        return String.format("%.2f %s", amountMinor / 100.0, currency);
    }
}
