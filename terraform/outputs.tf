output "webhook_url" {
  description = "Stripe should POST webhook events to this URL."
  value       = module.api_gateway.webhook_url
}

output "lambda_function_name" {
  description = "Deployed Lambda function name."
  value       = module.lambda.function_name
}

output "lambda_log_group" {
  description = "CloudWatch log group holding the Lambda's logs."
  value       = module.lambda.log_group_name
}

output "sqs_queue_url" {
  description = "Main event queue URL."
  value       = module.sqs.queue_url
}

output "sqs_dlq_url" {
  description = "Dead-letter queue URL — inspect here when retries are exhausted."
  value       = module.sqs.dlq_url
}

output "rds_endpoint" {
  description = "RDS endpoint (host:port)."
  value       = module.rds.endpoint
}

output "jdbc_url" {
  description = "JDBC URL consumed by the Lambda as DB_URL."
  value       = module.rds.jdbc_url
}

output "ssm_prefix" {
  description = "SSM Parameter Store prefix for this environment."
  value       = local.ssm_prefix
}
