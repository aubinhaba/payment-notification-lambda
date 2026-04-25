package com.bino.payment.notifier.repository;

import com.bino.payment.notifier.domain.NotificationStatus;
import com.bino.payment.notifier.domain.PaymentNotification;
import com.bino.payment.notifier.exception.PersistenceException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class PaymentNotificationRepositoryImpl implements PaymentNotificationRepository {

    private static final String INSERT_SQL = """
            INSERT INTO payment_notification
                (id, stripe_event_id, stripe_payment_intent_id,
                 amount, currency, customer_email,
                 notification_status, retry_count,
                 created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE payment_notification
               SET notification_status = ?,
                   retry_count         = ?,
                   updated_at          = ?
             WHERE id = ?
            """;

    private static final String SELECT_SQL = """
            SELECT id, stripe_event_id, stripe_payment_intent_id,
                   amount, currency, customer_email,
                   notification_status, retry_count,
                   created_at, updated_at
              FROM payment_notification
             WHERE stripe_event_id = ?
            """;

    private final DataSource dataSource;

    public PaymentNotificationRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<PaymentNotification> findByStripeEventId(String stripeEventId) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_SQL)) {
            ps.setString(1, stripeEventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Lookup failed for stripeEventId=" + stripeEventId, e);
        }
    }

    @Override
    public PaymentNotification insert(PaymentNotification entity) {
        if (entity.getId() == null) entity.setId(UUID.randomUUID());
        Instant now = Instant.now();
        if (entity.getCreatedAt() == null) entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT_SQL)) {
            ps.setObject(1, entity.getId());
            ps.setString(2, entity.getStripeEventId());
            ps.setString(3, entity.getStripePaymentIntentId());
            ps.setLong(4, entity.getAmount());
            ps.setString(5, entity.getCurrency());
            ps.setString(6, entity.getCustomerEmail());
            ps.setString(7, entity.getNotificationStatus().name());
            ps.setInt(8, entity.getRetryCount());
            ps.setTimestamp(9, Timestamp.from(entity.getCreatedAt()));
            ps.setTimestamp(10, Timestamp.from(entity.getUpdatedAt()));
            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new PersistenceException(
                    "Insert failed for stripeEventId=" + entity.getStripeEventId(), e);
        }
    }

    @Override
    public PaymentNotification update(PaymentNotification entity) {
        if (entity.getId() == null) {
            throw new IllegalArgumentException("Cannot update entity without id");
        }
        entity.setUpdatedAt(Instant.now());
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, entity.getNotificationStatus().name());
            ps.setInt(2, entity.getRetryCount());
            ps.setTimestamp(3, Timestamp.from(entity.getUpdatedAt()));
            ps.setObject(4, entity.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new PersistenceException("No rows updated for id=" + entity.getId());
            }
            return entity;
        } catch (SQLException e) {
            throw new PersistenceException("Update failed for id=" + entity.getId(), e);
        }
    }

    private static PaymentNotification mapRow(ResultSet rs) throws SQLException {
        return PaymentNotification.builder()
                .id((UUID) rs.getObject("id"))
                .stripeEventId(rs.getString("stripe_event_id"))
                .stripePaymentIntentId(rs.getString("stripe_payment_intent_id"))
                .amount(rs.getLong("amount"))
                .currency(rs.getString("currency"))
                .customerEmail(rs.getString("customer_email"))
                .notificationStatus(NotificationStatus.valueOf(rs.getString("notification_status")))
                .retryCount(rs.getInt("retry_count"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .build();
    }
}
