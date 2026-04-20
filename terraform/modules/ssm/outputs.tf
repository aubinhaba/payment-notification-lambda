output "parameter_arns" {
  description = "ARNs of all SSM parameters owned by this module — wire these into the Lambda IAM policy."
  value = concat(
    [
      aws_ssm_parameter.db_password.arn,
      aws_ssm_parameter.stripe_webhook_secret.arn,
    ],
    aws_ssm_parameter.stripe_api_key[*].arn,
  )
}
