CREATE TABLE IF NOT EXISTS payment_notification (
    id                        UUID         PRIMARY KEY,
    stripe_event_id           VARCHAR(255) NOT NULL,
    stripe_payment_intent_id  VARCHAR(255) NOT NULL,
    amount                    BIGINT       NOT NULL,
    currency                  VARCHAR(3)   NOT NULL,
    customer_email            VARCHAR(255) NOT NULL,
    notification_status       VARCHAR(20)  NOT NULL,
    retry_count               INT          NOT NULL DEFAULT 0,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payment_notification_stripe_event_id UNIQUE (stripe_event_id)
);
