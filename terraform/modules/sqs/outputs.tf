output "queue_arn" {
  description = "ARN of the main event queue — Lambda event source."
  value       = aws_sqs_queue.main.arn
}

output "queue_url" {
  description = "URL of the main queue — used by API Gateway SendMessage integration."
  value       = aws_sqs_queue.main.url
}

output "queue_name" {
  description = "Name of the main queue — referenced by the API Gateway integration path."
  value       = aws_sqs_queue.main.name
}

output "dlq_arn" {
  description = "ARN of the dead-letter queue."
  value       = aws_sqs_queue.dlq.arn
}

output "dlq_url" {
  description = "URL of the dead-letter queue."
  value       = aws_sqs_queue.dlq.url
}
