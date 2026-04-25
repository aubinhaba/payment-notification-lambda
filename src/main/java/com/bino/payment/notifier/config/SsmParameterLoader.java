package com.bino.payment.notifier.config;

import com.bino.payment.notifier.exception.ConfigurationException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

/**
 * Loads configuration values from AWS SSM Parameter Store.
 *
 * <p>Secrets ({@code stripe.webhook.secret}, {@code stripe.api.key}, {@code db.password}) are
 * stored as SecureString parameters under a per-environment prefix
 * (e.g. {@code /stripe-payment-notifier/prod/stripe/webhook-secret}) and decrypted on read.
 */
public class SsmParameterLoader {

    private final SsmClient client;
    private final String prefix;

    public SsmParameterLoader(SsmClient client, String prefix) {
        this.client = client;
        this.prefix = normalise(prefix);
    }

    public String requireSecure(String relativeName) {
        return require(relativeName, true);
    }

    public String require(String relativeName) {
        return require(relativeName, false);
    }

    private String require(String relativeName, boolean decrypt) {
        String fullName = prefix + relativeName;
        try {
            return client.getParameter(GetParameterRequest.builder()
                            .name(fullName)
                            .withDecryption(decrypt)
                            .build())
                    .parameter()
                    .value();
        } catch (ParameterNotFoundException e) {
            throw new ConfigurationException("SSM parameter not found: " + fullName, e);
        } catch (RuntimeException e) {
            throw new ConfigurationException("Failed to read SSM parameter: " + fullName, e);
        }
    }

    private static String normalise(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("SSM parameter prefix must be provided");
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }
}
