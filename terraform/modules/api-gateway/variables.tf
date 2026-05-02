variable "project" {
  description = "Project + environment slug used in resource names."
  type        = string
}

variable "aws_region" {
  description = "AWS region — used to build the SQS integration URI."
  type        = string
}

variable "stage_name" {
  description = "API Gateway stage name (appears in the invoke URL path)."
  type        = string
  default     = "prod"
}

variable "sqs_queue_arn" {
  description = "ARN of the queue the webhook route pushes into."
  type        = string
}

variable "sqs_queue_name" {
  description = "Name of the queue — used in the SQS integration URI path."
  type        = string
}

variable "log_retention_days" {
  description = "CloudWatch retention for API Gateway access logs."
  type        = number
  default     = 14
}

variable "apigw_account_id" {
  description = "cloudwatch_role_arn de aws_api_gateway_account — garantit que le role CloudWatch est configure avant le stage."
  type        = string
}
