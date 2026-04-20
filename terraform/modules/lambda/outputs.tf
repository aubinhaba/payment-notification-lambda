output "function_name" {
  description = "Name of the deployed Lambda function."
  value       = aws_lambda_function.this.function_name
}

output "function_arn" {
  description = "ARN of the Lambda function (unqualified)."
  value       = aws_lambda_function.this.arn
}

output "alias_arn" {
  description = "ARN of the live alias — this is what SnapStart restores from."
  value       = aws_lambda_alias.live.arn
}

output "role_arn" {
  description = "ARN of the Lambda execution role."
  value       = aws_iam_role.this.arn
}

output "log_group_name" {
  description = "CloudWatch log group name."
  value       = aws_cloudwatch_log_group.this.name
}
