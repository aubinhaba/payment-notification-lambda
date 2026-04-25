package com.bino.payment.notifier.repository;

import com.bino.payment.notifier.domain.NotificationStatus;
import com.bino.payment.notifier.domain.PaymentNotification;
import com.bino.payment.notifier.exception.PersistenceException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentNotificationRepositoryImplTest {

    private JdbcDataSource dataSource;
    private PaymentNotificationRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:notifier;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        String schema = new String(
                getClass().getResourceAsStream("/db/schema-h2.sql").readAllBytes(),
                StandardCharsets.UTF_8);
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute(schema);
        }
        repository = new PaymentNotificationRepositoryImpl(dataSource);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("DROP ALL OBJECTS");
        }
    }

    @Test
    void insert_persistsAndAssignsId() {
        PaymentNotification saved = repository.insert(newPending("evt_1", "pi_1", "alice@example.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<PaymentNotification> loaded = repository.findByStripeEventId("evt_1");
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getCustomerEmail()).isEqualTo("alice@example.com");
        assertThat(loaded.get().getRetryCount()).isZero();
    }

    @Test
    void insert_enforcesUniqueStripeEventIdConstraint() {
        repository.insert(newPending("evt_dup", "pi_1", "a@example.com"));

        assertThatThrownBy(() -> repository.insert(newPending("evt_dup", "pi_2", "b@example.com")))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    void update_changesStatusAndRetryCount() {
        PaymentNotification saved = repository.insert(newPending("evt_upd", "pi_1", "c@example.com"));
        saved.setNotificationStatus(NotificationStatus.SENT);
        saved.setRetryCount(1);

        repository.update(saved);

        PaymentNotification reloaded = repository.findByStripeEventId("evt_upd").orElseThrow();
        assertThat(reloaded.getNotificationStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(reloaded.getRetryCount()).isEqualTo(1);
    }

    @Test
    void findByStripeEventId_returnsEmptyWhenMissing() {
        assertThat(repository.findByStripeEventId("evt_nope")).isEmpty();
    }

    private static PaymentNotification newPending(String eventId, String paymentIntentId, String email) {
        return PaymentNotification.builder()
                .stripeEventId(eventId)
                .stripePaymentIntentId(paymentIntentId)
                .amount(1999L)
                .currency("EUR")
                .customerEmail(email)
                .notificationStatus(NotificationStatus.PENDING)
                .retryCount(0)
                .build();
    }
}
