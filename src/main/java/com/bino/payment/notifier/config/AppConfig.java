package com.bino.payment.notifier.config;

import com.bino.payment.notifier.channel.NotificationChannel;
import com.bino.payment.notifier.channel.SesEmailChannel;
import com.bino.payment.notifier.exception.ConfigurationException;
import com.bino.payment.notifier.repository.PaymentNotificationRepository;
import com.bino.payment.notifier.repository.PaymentNotificationRepositoryImpl;
import com.bino.payment.notifier.service.NotificationService;
import com.bino.payment.notifier.stripe.StripeWebhookValidator;
import lombok.Getter;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.ssm.SsmClient;

import javax.sql.DataSource;

/**
 * Lazy-initialised singleton that wires the Lambda's dependencies during the INIT phase so
 * SnapStart can snapshot the warmed state.
 *
 * <p>Only non-secret runtime knobs come from environment variables
 * ({@code AWS_REGION}, {@code SSM_PARAMETER_PREFIX}, {@code NOTIFICATION_FROM_EMAIL},
 * {@code DB_URL}, {@code DB_USER}). Secrets are read from SSM Parameter Store.
 */
@Getter
public final class AppConfig {

    private static volatile AppConfig instance;

    private final StripeWebhookValidator stripeWebhookValidator;
    private final NotificationChannel notificationChannel;
    private final NotificationService notificationService;

    private AppConfig() {
        String region = requireEnv("AWS_REGION");
        String ssmPrefix = requireEnv("SSM_PARAMETER_PREFIX");
        String fromEmail = requireEnv("NOTIFICATION_FROM_EMAIL");
        String dbUrl = requireEnv("DB_URL");
        String dbUser = requireEnv("DB_USER");
        String configurationSet = System.getenv("SES_CONFIGURATION_SET");

        SdkHttpClient http = UrlConnectionHttpClient.builder().build();
        Region awsRegion = Region.of(region);

        SsmClient ssmClient = SsmClient.builder()
                .region(awsRegion).httpClient(http).build();
        SsmParameterLoader ssm = new SsmParameterLoader(ssmClient, ssmPrefix);

        String dbPassword = ssm.requireSecure("db/password");
        String stripeWebhookSecret = ssm.requireSecure("stripe/webhook-secret");

        SesV2Client sesClient = SesV2Client.builder()
                .region(awsRegion).httpClient(http).build();

        DataSource dataSource = DataSourceFactory.create(dbUrl, dbUser, dbPassword);
        PaymentNotificationRepository repository = new PaymentNotificationRepositoryImpl(dataSource);

        this.stripeWebhookValidator = new StripeWebhookValidator(stripeWebhookSecret);
        this.notificationChannel = new SesEmailChannel(sesClient, fromEmail, configurationSet);
        this.notificationService = new NotificationService(repository, notificationChannel);
    }

    public static AppConfig getInstance() {
        AppConfig local = instance;
        if (local == null) {
            synchronized (AppConfig.class) {
                local = instance;
                if (local == null) {
                    local = new AppConfig();
                    instance = local;
                }
            }
        }
        return local;
    }

    private static String requireEnv(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new ConfigurationException("Missing required environment variable: " + key);
        }
        return v;
    }
}
