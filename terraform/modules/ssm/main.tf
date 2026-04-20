resource "aws_ssm_parameter" "db_password" {
  name        = "${var.ssm_prefix}/db/password"
  description = "PostgreSQL master password for the notifier database."
  type        = "SecureString"
  value       = var.db_password
}

resource "aws_ssm_parameter" "stripe_webhook_secret" {
  name        = "${var.ssm_prefix}/stripe/webhook-secret"
  description = "Stripe endpoint signing secret used to verify inbound webhooks."
  type        = "SecureString"
  value       = var.stripe_webhook_secret
}

resource "aws_ssm_parameter" "stripe_api_key" {
  count       = var.stripe_api_key == "" ? 0 : 1
  name        = "${var.ssm_prefix}/stripe/api-key"
  description = "Stripe server-side API key."
  type        = "SecureString"
  value       = var.stripe_api_key
}
