output "from_identity_arn" {
  description = "ARN of the verified From: email identity."
  value       = aws_sesv2_email_identity.from.arn
}

output "configuration_set_name" {
  description = "Name of the SES configuration set — exposed to Lambda as SES_CONFIGURATION_SET."
  value       = aws_sesv2_configuration_set.this.configuration_set_name
}
