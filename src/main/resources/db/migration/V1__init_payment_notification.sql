CREATE TABLE IF NOT EXISTS payment_notification (
    id                        UUID         PRIMARY KEY,
    stripe_event_id           VARCHAR(255) NOT NULL,
    stripe_payment_intent_id  VARCHAR(255) NOT NULL,
    amount                    BIGINT       NOT NULL,
    currency                  VARCHAR(3)   NOT NULL,
    customer_email            VARCHAR(255) NOT NULL,
    notification_status       VARCHAR(20)  NOT NULL,
    retry_count               INTEGER      NOT NULL DEFAULT 0,
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payment_notification_stripe_event_id UNIQUE (stripe_event_id)
);

CREATE INDEX IF NOT EXISTS idx_payment_notification_status         ON payment_notification (notification_status);
CREATE INDEX IF NOT EXISTS idx_payment_notification_created_at     ON payment_notification (created_at);
CREATE INDEX IF NOT EXISTS idx_payment_notification_payment_intent ON payment_notification (stripe_payment_intent_id);
