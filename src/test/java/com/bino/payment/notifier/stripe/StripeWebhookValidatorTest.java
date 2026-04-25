package com.bino.payment.notifier.stripe;

import com.bino.payment.notifier.exception.StripeSignatureException;
import com.stripe.model.Event;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StripeWebhookValidatorTest {

    private static final String SECRET = "whsec_test_secret";
    private static final String VALID_PAYLOAD = """
            {"id":"evt_test","object":"event","type":"payment_intent.succeeded",
             "data":{"object":{"id":"pi_test","object":"payment_intent",
             "amount":1000,"currency":"eur","receipt_email":"x@example.com"}}}""";

    @Test
    void validate_acceptsGenuineSignature() throws Exception {
        StripeWebhookValidator validator = new StripeWebhookValidator(SECRET);
        Event event = validator.validate(VALID_PAYLOAD, signatureHeaderFor(VALID_PAYLOAD));

        assertThat(event.getId()).isEqualTo("evt_test");
        assertThat(event.getType()).isEqualTo("payment_intent.succeeded");
    }

    @Test
    void validate_rejectsTamperedPayload() throws Exception {
        String header = signatureHeaderFor(VALID_PAYLOAD);
        StripeWebhookValidator validator = new StripeWebhookValidator(SECRET);
        String tampered = VALID_PAYLOAD.replace("1000", "9999");

        assertThatThrownBy(() -> validator.validate(tampered, header))
                .isInstanceOf(StripeSignatureException.class);
    }

    @Test
    void validate_rejectsMissingHeader() {
        StripeWebhookValidator validator = new StripeWebhookValidator(SECRET);
        assertThatThrownBy(() -> validator.validate(VALID_PAYLOAD, null))
                .isInstanceOf(StripeSignatureException.class);
    }

    @Test
    void constructor_rejectsBlankSecret() {
        assertThatThrownBy(() -> new StripeWebhookValidator(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static String signatureHeaderFor(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signed = HexFormat.of()
                .formatHex(mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8)));
        return "t=" + timestamp + ",v1=" + signed;
    }
}
