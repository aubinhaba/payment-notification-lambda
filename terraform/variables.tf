variable "aws_region" {
  description = "AWS region where all resources are deployed."
  type        = string
  default     = "eu-west-1"
}

variable "environment" {
  description = "Deployment environment identifier — used in names and the SSM prefix."
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "environment must be one of: dev, staging, prod."
  }
}

variable "project_name" {
  description = "Short slug used as prefix for AWS resource names."
  type        = string
  default     = "stripe-payment-notifier"
}

variable "lambda_jar_path" {
  description = "Absolute or relative path to the shaded Lambda JAR produced by `mvn package`."
  type        = string
  default     = "../target/stripe-payment-notifier-1.0.0.jar"
}

variable "notification_from_email" {
  description = "Verified SES identity used as the From: address for customer notifications."
  type        = string
}

variable "stripe_webhook_secret" {
  description = "Stripe endpoint signing secret (stored as SSM SecureString, never committed)."
  type        = string
  sensitive   = true
}

variable "stripe_api_key" {
  description = "Stripe API key for server-side calls (stored as SSM SecureString)."
  type        = string
  sensitive   = true
  default     = ""
}

variable "db_username" {
  description = "PostgreSQL master username."
  type        = string
  default     = "notifier"
}

variable "db_password" {
  description = "PostgreSQL master password (stored as SSM SecureString, never committed)."
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "PostgreSQL database name."
  type        = string
  default     = "stripe_notifier"
}

variable "vpc_cidr" {
  description = "IPv4 CIDR block for the VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "log_retention_days" {
  description = "CloudWatch log retention in days for the Lambda log group."
  type        = number
  default     = 14
}
