variable "project" {
  description = "Project + environment slug used in resource names."
  type        = string
}

variable "visibility_timeout_seconds" {
  description = "SQS visibility timeout — must be ≥ the Lambda timeout."
  type        = number
  default     = 60
}

variable "max_receive_count" {
  description = "Receive attempts before a message is redriven to the DLQ."
  type        = number
  default     = 3
}

variable "message_retention_seconds" {
  description = "How long the main queue keeps an unprocessed message."
  type        = number
  default     = 345600 # 4 days
}
