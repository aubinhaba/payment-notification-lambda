variable "project" {
  description = "Project + environment slug used in resource names."
  type        = string
}

variable "lambda_name" {
  description = "Full name of the Lambda function."
  type        = string
}

variable "jar_path" {
  description = "Local path to the shaded Lambda JAR."
  type        = string
}

variable "handler" {
  description = "Fully-qualified Lambda handler class."
  type        = string
  default     = "com.bino.payment.notifier.handler.StripeWebhookHandler"
}

variable "memory_mb" {
  description = "Lambda memory allocation — CPU scales with memory."
  type        = number
  default     = 1024
}

variable "timeout_seconds" {
  description = "Lambda timeout — must be ≤ the SQS visibility timeout."
  type        = number
  default     = 30
}

variable "log_retention_days" {
  description = "CloudWatch log retention."
  type        = number
}

variable "environment_variables" {
  description = "Non-secret env vars exposed to the function."
  type        = map(string)
}

variable "subnet_ids" {
  description = "Private subnet IDs for Lambda ENIs."
  type        = list(string)
}

variable "security_group_id" {
  description = "Security group attached to Lambda ENIs."
  type        = string
}

variable "sqs_queue_arn" {
  description = "ARN of the SQS queue triggering the Lambda."
  type        = string
}

variable "ssm_parameter_arns" {
  description = "ARNs of the SSM parameters the Lambda is allowed to read."
  type        = list(string)
}

variable "ses_configuration_set_arn" {
  description = "ARN of the SES configuration set (for fine-grained SendEmail permission)."
  type        = string
  default     = ""
}

variable "batch_size" {
  description = "SQS → Lambda batch size."
  type        = number
  default     = 1
}

variable "notification_from_email" {
  description = "Verified SES identity used as the From: address for customer notifications."
  type        = string
}
