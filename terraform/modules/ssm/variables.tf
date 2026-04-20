variable "ssm_prefix" {
  description = "Parameter Store path prefix, e.g. /stripe-payment-notifier/prod."
  type        = string
}

variable "db_password" {
  description = "PostgreSQL master password — stored as SecureString."
  type        = string
  sensitive   = true
}

variable "stripe_webhook_secret" {
  description = "Stripe endpoint signing secret — stored as SecureString."
  type        = string
  sensitive   = true
}

variable "stripe_api_key" {
  description = "Stripe API key — stored as SecureString. Empty string skips creation."
  type        = string
  sensitive   = true
  default     = ""
}
