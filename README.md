# stripe-payment-notifier

AWS Lambda that listens to Stripe `payment_intent.succeeded` webhooks and sends a transactional confirmation email to the customer via AWS SES.

## Architecture

```
Stripe ──► API Gateway (POST /webhook)
                │
                ▼  SQS-SendMessage (direct, no Lambda in hot path)
           SQS queue ──► DLQ (after 3 retries)
                │
                ▼
        Lambda  (Java 21, SnapStart, VPC)
           │                    │
           ▼                    ▼
      PostgreSQL RDS        AWS SES v2
      (idempotency)         (email)
```

## Stack

| | |
|---|---|
| Runtime | Java 21, Lambda SnapStart |
| Build | Maven, shaded JAR |
| AWS SDK | v2 — `SesV2Client`, `SsmClient`, `UrlConnectionHttpClient` |
| DB | Pure JDBC + HikariCP (no Spring) |
| Stripe | `stripe-java` SDK |
| Infra | Terraform 1.6+, per-resource modules |
| Tests | JUnit 5 + Mockito + H2 |

## Build & deploy

```bash
# Build
mvn clean package -DskipTests

# Deploy
cd terraform
cp terraform.tfvars.example terraform.tfvars   # fill in secrets
terraform init
terraform apply -var environment=dev
```

## Configuration

Non-secret env vars are set by Terraform (`SSM_PARAMETER_PREFIX`, `NOTIFICATION_FROM_EMAIL`, `DB_URL`, `DB_USER`, `SES_CONFIGURATION_SET`).

Secrets are read from SSM Parameter Store at cold start:

| Parameter | |
|---|---|
| `{prefix}/db/password` | PostgreSQL password |
| `{prefix}/stripe/webhook-secret` | Stripe endpoint signing secret |
| `{prefix}/stripe/api-key` | Stripe API key (optional) |

## Tests

```bash
mvn test
```
