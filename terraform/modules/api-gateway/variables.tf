variable "project" {
  description = "Project + environment slug used in resource names."
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

variable "sqs_queue_url" {
  description = "URL of the queue — used by the AWS-proxy SendMessage integration."
  type        = string
}

variable "log_retention_days" {
  description = "CloudWatch retention for API Gateway access logs."
  type        = number
  default     = 14
}
