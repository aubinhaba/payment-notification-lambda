package com.bino.payment.notifier.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotification {

    private UUID id;
    private String stripeEventId;
    private String stripePaymentIntentId;
    private long amount;
    private String currency;
    private String customerEmail;
    private NotificationStatus notificationStatus;
    private int retryCount;
    private Instant createdAt;
    private Instant updatedAt;
}
